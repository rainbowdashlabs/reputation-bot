package de.chojo.repbot.dao.access.guild.reputation.sub;

import de.chojo.repbot.analyzer.ThankType;
import de.chojo.repbot.dao.access.guild.reputation.Reputation;
import de.chojo.repbot.dao.access.guild.reputation.sub.user.Gdpr;
import de.chojo.repbot.dao.components.MemberHolder;
import de.chojo.repbot.dao.snapshots.RepProfile;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class RepUser extends QueryFactoryHolder implements MemberHolder {
    private static final Logger log = getLogger(RepUser.class);
    private final Reputation reputation;
    private final Gdpr gdpr;
    private final User user;
    private Member member;

    public RepUser(Reputation reputation, Member member) {
        super(reputation);
        gdpr = new Gdpr(this);
        this.reputation = reputation;
        this.member = member;
        user = member.getUser();
    }

    public RepUser(Reputation reputation, User user) {
        super(reputation);
        gdpr = new Gdpr(this);
        this.reputation = reputation;
        this.user = user;
    }

    public Gdpr gdpr() {
        return gdpr;
    }

    /**
     * Log reputation for a user.
     *
     * @param donor      donator of the reputation
     * @param message    message to log
     * @param refMessage reference message if available
     * @param type       type of reputation
     * @return true if the repuation was logged.
     */
    public boolean addReputation(@Nullable Member donor, @NotNull Message message, @Nullable Message refMessage, ThankType type) {
        var success = builder()
                              .query("""
                                      INSERT INTO
                                      reputation_log(guild_id, donor_id, receiver_id, message_id, ref_message_id, channel_id, cause) VALUES(?,?,?,?,?,?,?)
                                          ON CONFLICT(guild_id, donor_id, receiver_id, message_id)
                                              DO NOTHING;
                                      """)
                              .paramsBuilder(stmt -> stmt.setLong(guildId()).setLong(donor == null ? 0 : donor.getIdLong()).setLong(userId())
                                      .setLong(message.getIdLong()).setLong(refMessage == null ? null : refMessage.getIdLong())
                                      .setLong(message.getChannel().getIdLong()).setString(type.name()))
                              .insert()
                              .executeSync() > 0;
        if (success) {
            log.debug("{} received one reputation from {} for message {}", user().getName(), donor != null ? donor.getEffectiveName() : "unkown", message.getIdLong());
        }
        return success;
    }


    /**
     * Get the last time where the the user gave reputation to the user or received reputation from this user
     *
     * @param other the other user
     * @return last timestamp as instant
     */
    public Optional<Instant> getLastReputation(Member other) {
        return builder(Instant.class).
                query("""
                        SELECT
                            received
                        FROM
                            reputation_log
                        WHERE
                            guild_id = ?
                            AND ((donor_id = ? AND receiver_id = ?)
                                OR (donor_id = ? AND receiver_id = ?))
                        ORDER BY received DESC
                        LIMIT  1;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(reputation.guildId())
                        .setLong(userId()).setLong(other.getIdLong()).
                        setLong(other.getIdLong()).setLong(userId()))
                .readRow(row -> row.getTimestamp("received").toInstant())
                .firstSync();
    }

    /**
     * Get the time since the last reputation.
     *
     * @param other receiver
     * @return the time since the last vote in the requested time unit or 1 year if no entry was found.
     */
    public Duration getLastRatedDuration(Member other) {
        return getLastReputation(other).map(last -> Duration.between(last, Instant.now())).orElseGet(() -> Duration.ofDays(365));
    }

    /**
     * Get the reputation user.
     *
     * @return the reputation user
     */
    public RepProfile profile() {
        // We probably dont want to cache the profile. There are just too many factors which can change the user reputation.
        return builder(RepProfile.class)
                .query("""
                        SELECT rank, user_id, reputation FROM user_reputation WHERE guild_id = ? AND user_id = ?;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guildId()).setLong(userId()))
                .readRow(RepProfile::build)
                .firstSync()
                .orElseGet(() -> RepProfile.empty(user()));
    }

    @Override
    public Member member() {
        return member;
    }

    @Override
    public User user() {
        return user;
    }

    @Override
    public Guild guild() {
        return reputation.guild();
    }

    public RepUser refresh(Member member) {
        this.member = member;
        return this;
    }
}
