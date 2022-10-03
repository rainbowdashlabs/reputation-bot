package de.chojo.repbot.dao.access.gdpr;

import de.chojo.repbot.dao.access.Gdpr;
import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.wrapper.util.Row;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class GdprUser extends QueryFactory {

    private static final Logger log = getLogger(GdprUser.class);
    private final User user;

    public GdprUser(Gdpr gdpr, User user) {
        super(gdpr);
        this.user = user;
    }

    @Nullable
    public static GdprUser build(Gdpr gdpr, Row rs, ShardManager shardManager) throws SQLException {
        try {
            var user = shardManager.retrieveUserById(rs.getLong("user_id")).complete();
            if (user == null) {
                log.info("Could not process gdpr request for user {}. User could not be retrieved.", rs.getLong("user_id"));
                return null;
            }

            return new GdprUser(gdpr, user);
        } catch (RuntimeException e) {
            log.info("Could not process gdpr request for user {}. User could not be retrieved.", rs.getLong("user_id"));
            return null;
        }
    }

    public boolean queueDeletion() {
        return builder()
                .query("""
                       INSERT INTO
                           cleanup_schedule(user_id, delete_after)
                           VALUES (?, NOW())
                               ON CONFLICT(guild_id, user_id)
                                   DO NOTHING;
                       """)
                .parameter(stmt -> stmt.setLong(userId()))
                .update()
                .sendSync()
                .changed();
    }

    public boolean request() {
        return builder()
                .query("""
                       DELETE FROM gdpr_log
                       WHERE user_id = ?
                           AND received IS NOT NULL
                           AND received < NOW() - INTERVAL '30 days';
                       """)
                .parameter(stmt -> stmt.setLong(userId()))
                .append()
                .query("""
                       INSERT INTO gdpr_log(user_id) VALUES(?)
                           ON CONFLICT(user_id)
                               DO NOTHING;
                       """)
                .parameter(stmt -> stmt.setLong(userId()))
                .update()
                .sendSync()
                .changed();
    }

    public void requestSend() {
        builder()
                .query("UPDATE gdpr_log SET received = NOW(), last_attempt = NOW() WHERE user_id = ?")
                .parameter(stmt -> stmt.setLong(userId()))
                .update()
                .sendSync();
    }

    public void requestSendFailed() {
        builder()
                .query("UPDATE gdpr_log SET attempts = attempts + 1, last_attempt = NOW() WHERE user_id = ?")
                .parameter(stmt -> stmt.setLong(userId()))
                .update()
                .sendSync();
    }

    public Optional<String> userData() {
        return builder(String.class)
                .query("SELECT aggregate_user_data(?)")
                .parameter(stmt -> stmt.setLong(userId()))
                .readRow(rs -> rs.getString(1))
                .firstSync();
    }

    /**
     * Sends the data of the user to the user via private message.
     * <p>
     * Sending of the message can fail for multiple reasons:
     * <p>
     * - The user does no longer share a Guild with the user, which will make the bot unable to retrieve the user.
     * <p>
     * - The user has closed private messages
     * <p>
     * - The data aggregation failed
     * <p>
     * - The temp file to write the data could not be written
     * <p>
     * - The file exceeds the max file size
     *
     * @return true when the request was sent.
     */
    public boolean sendData() {
        PrivateChannel privateChannel;
        try {
            privateChannel = user.openPrivateChannel().complete();
        } catch (RuntimeException e) {
            log.info("Could not process gdpr request for user {}. Could not open private channel.", userId());
            return false;
        }
        if (privateChannel == null) {
            log.info("Could not process gdpr request for user {}. Could not open private channel.", userId());
            return false;
        }

        var userData = userData();

        if (userData.isEmpty()) {
            log.warn("Could not process gdpr request for user {}. Data aggregation failed.", userId());
            return false;
        }
        Path tempFile;
        try {
            tempFile = Files.createTempFile("repbot_gdpr", ".json");
        } catch (IOException e) {
            log.warn("Coult not create temp file", e);
            return false;
        }
        try {
            Files.writeString(tempFile, userData.get());
        } catch (IOException e) {
            log.warn("Could not write to temp file", e);
            return false;
        }

        try {
            privateChannel
                    .sendMessage("Here is your requested data. You can request it again in 30 days.")
                    .addFiles(FileUpload.fromData(tempFile.toFile()))
                    .complete();
        } catch (RuntimeException e) {
            log.warn("Could not send gdpr data to user {}. File sending failed.", userId(), e);
            return false;
        }
        return true;
    }

    private long userId() {
        return user.getIdLong();
    }
}
