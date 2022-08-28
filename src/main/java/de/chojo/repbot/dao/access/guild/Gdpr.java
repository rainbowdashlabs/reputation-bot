package de.chojo.repbot.dao.access.guild;

import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Guild;

public class Gdpr extends QueryFactory implements GuildHolder {
    private final RepGuild repGuild;

    public Gdpr(RepGuild repGuild) {
        super(repGuild);
        this.repGuild = repGuild;
    }

    @Override
    public Guild guild() {
        return repGuild.guild();
    }

    public void queueDeletion() {
        builder()
                .query("""
                       INSERT INTO
                           cleanup_schedule(guild_id)
                           VALUES (?)
                               ON CONFLICT(guild_id, user_id)
                                   DO NOTHING;
                       """)
                .parameter(stmt -> stmt.setLong(guildId()))
                .update()
                .sendSync();
    }

    public void dequeueDeletion() {
        builder()
                .query("DELETE FROM cleanup_schedule WHERE guild_id = ?;")
                .parameter(stmt -> stmt.setLong(guildId()))
                .update()
                .sendSync();
    }
}
