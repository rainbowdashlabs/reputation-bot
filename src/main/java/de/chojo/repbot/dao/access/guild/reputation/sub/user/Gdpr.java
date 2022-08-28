package de.chojo.repbot.dao.access.guild.reputation.sub.user;

import de.chojo.repbot.dao.access.guild.reputation.sub.RepUser;
import de.chojo.repbot.dao.components.MemberHolder;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class Gdpr extends QueryFactory implements MemberHolder {
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
                .parameter(stmt -> stmt.setLong(guildId()).setLong(userId()))
                .update()
                .sendSync();
    }

    public void dequeueDeletion() {
        builder()
                .query("""
                       DELETE FROM
                           cleanup_schedule
                       WHERE guild_id = ?
                           AND user_id = ?;
                       """)
                .parameter(stmt -> stmt.setLong(guildId()).setLong(userId()))
                .update()
                .sendSync();
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
