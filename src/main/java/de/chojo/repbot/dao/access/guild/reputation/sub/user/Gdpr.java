package de.chojo.repbot.dao.access.guild.reputation.sub.user;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.reputation.sub.RepUser;
import de.chojo.repbot.dao.components.MemberHolder;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Gdpr extends QueryFactory implements MemberHolder {
    private final RepUser repUser;
    private static final Logger log = getLogger(Gdpr.class);

    public Gdpr(RepUser repUser) {
        super(repUser);
        this.repUser = repUser;
    }

    @Override
    public Member member() {
        return repUser.member();
    }

    public void queueDeletion() {
        log.info("User {} is scheduled for deletion on guild {}", userId(), guildId());
        builder()
                .query("""
                       INSERT INTO
                           cleanup_schedule(guild_id, user_id, delete_after)
                           VALUES (?,?,now() + ?::INTERVAL)
                               ON CONFLICT(guild_id, user_id)
                                   DO NOTHING;
                       """, repUser.configuration().cleanup().cleanupScheduleDays())
                .parameter(stmt -> stmt.setLong(guildId())
                                       .setLong(userId())
                                       .setString("%d DAYS".formatted(repUser.configuration().cleanup().cleanupScheduleDays())))
                .update()
                .sendSync();
    }

    public void dequeueDeletion() {
        if (builder()
                .query("""
                       DELETE FROM
                           cleanup_schedule
                       WHERE guild_id = ?
                           AND user_id = ?;
                       """)
                .parameter(stmt -> stmt.setLong(guildId()).setLong(userId()))
                .update()
                .sendSync()
                .changed()) {
            log.info("User {} deletion on guild {} canceled", userId(), guildId());
        }
    }

    @Override
    public User user() {
        return repUser.user();
    }

    @Override
    public Guild guild() {
        return repUser.guild();
    }
}
