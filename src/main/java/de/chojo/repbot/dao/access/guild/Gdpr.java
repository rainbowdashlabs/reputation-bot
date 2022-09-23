package de.chojo.repbot.dao.access.guild;

import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Gdpr extends QueryFactory implements GuildHolder {
    private final RepGuild repGuild;
    private static final Logger log = getLogger(Gdpr.class);

    public Gdpr(RepGuild repGuild) {
        super(repGuild);
        this.repGuild = repGuild;
    }

    @Override
    public Guild guild() {
        return repGuild.guild();
    }

    @Override
    public long guildId() {
        return repGuild.guildId();
    }

    public void queueDeletion() {
        if (builder()
                .query("""
                       INSERT INTO
                           cleanup_schedule(guild_id, user_id)
                           VALUES (?, 0)
                               ON CONFLICT(guild_id, user_id)
                                   DO NOTHING;
                       """)
                .parameter(stmt -> stmt.setLong(guildId()))
                .update()
                .sendSync()
                .changed()) {
            log.debug("Queuing guild {} for deletion.", guildId());
        }
    }

    public void dequeueDeletion() {
        if (builder()
                .query("DELETE FROM cleanup_schedule WHERE guild_id = ? AND user_id IS NULL;")
                .parameter(stmt -> stmt.setLong(guildId()))
                .update()
                .sendSync()
                .changed()) {
            log.debug("Deletion of guild {} canceled.", guildId());
        }
    }
}
