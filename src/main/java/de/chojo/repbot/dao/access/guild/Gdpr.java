package de.chojo.repbot.dao.access.guild;

import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import net.dv8tion.jda.api.entities.Guild;

public class Gdpr extends QueryFactoryHolder implements GuildHolder {
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
                .paramsBuilder(stmt -> stmt.setLong(guildId()))
                .update().executeSync();
    }

    public void dequeueDeletion() {
        builder()
                .query("DELETE FROM cleanup_schedule WHERE guild_id = ?;")
                .paramsBuilder(stmt -> stmt.setLong(guildId()))
                .update().executeSync();
    }
}
