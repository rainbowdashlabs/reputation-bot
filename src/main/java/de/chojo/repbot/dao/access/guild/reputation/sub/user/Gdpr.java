package de.chojo.repbot.dao.access.guild.reputation.sub.user;

import de.chojo.repbot.dao.access.guild.reputation.sub.RepUser;
import de.chojo.repbot.dao.components.MemberHolder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import net.dv8tion.jda.api.entities.Member;

public class Gdpr extends QueryFactoryHolder implements MemberHolder {
    private final RepUser repUser;

    public Gdpr(RepUser repUser) {
        super(repUser);
        this.repUser = repUser;
    }

    @Override
    public Member member() {
        return repUser.member();
    }

    public void queueDeletion() {
        builder()
                .query("""
                        INSERT INTO
                            cleanup_schedule(guild_id, user_id)
                            VALUES (?,?)
                                ON CONFLICT(guild_id, user_id)
                                    DO NOTHING;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guildId()).setLong(userId()))
                .update().executeSync();
    }

    public void dequeueDeletion() {
        builder()
                .query("""
                        DELETE FROM
                            cleanup_schedule
                        WHERE guild_id = ?
                            AND user_id = ?;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guildId()).setLong(userId()))
                .update().executeSync();
    }
}
