package de.chojo.repbot.dao.access.guild;

import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import net.dv8tion.jda.api.entities.Guild;

public class Migration extends QueryFactoryHolder implements GuildHolder {
    private final RepGuild repGuild;

    public Migration(RepGuild repGuild) {
        super(repGuild);
        this.repGuild = repGuild;
    }

    public void promptMigration() {
        builder().query("""
                        INSERT INTO migrations(guild_id) VALUES(?) ON CONFLICT DO NOTHING
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guildId()))
                .update().executeSync();
    }

    public boolean migrationActive() {
        return builder(Boolean.class).query("""
                        SELECT EXISTS(SELECT 1 FROM migrations WHERE guild_id = ?) AS exists
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guildId()))
                .readRow(rs -> rs.getBoolean("exists"))
                .firstSync().get();
    }

    public void migrated() {
        builder(Integer.class).query("""
                        UPDATE migrations SET migrated = NOW() WHERE guild_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guildId()))
                .update().execute();
    }

    @Override
    public Guild guild() {
        return repGuild.guild();
    }
}
