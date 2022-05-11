package de.chojo.repbot.dao.access.guild;

import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import net.dv8tion.jda.api.entities.Guild;

import java.time.LocalDateTime;
import java.util.Optional;

public class Cleanup extends QueryFactoryHolder implements GuildHolder {
    private RepGuild repGuild;

    public Cleanup(RepGuild repGuild) {
        super(repGuild);
        this.repGuild = repGuild;
    }

    public void selfCleanupPrompt() {
        builder().query("""
                        INSERT INTO self_cleanup(guild_id) VALUES(?)
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guildId()))
                .update().executeSync();
    }

    public Optional<LocalDateTime> getCleanupPromptTime() {
        return builder(LocalDateTime.class)
                .query("""
                        SELECT prompted FROM self_cleanup WHERE guild_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guildId()))
                .readRow(rs -> rs.getTimestamp("prompted").toLocalDateTime())
                .firstSync();
    }

    public void cleanupDone() {
        builder(Boolean.class).query("""
                                DELETE FROM self_cleanup WHERE guild_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guildId()))
                .update().executeSync();
    }

    @Override
    public Guild guild() {
        return repGuild.guild();
    }
}
