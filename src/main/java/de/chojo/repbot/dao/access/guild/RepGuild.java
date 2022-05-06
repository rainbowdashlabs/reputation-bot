package de.chojo.repbot.dao.access.guild;

import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import net.dv8tion.jda.api.entities.Guild;

import javax.sql.DataSource;
import java.util.List;

public class RepGuild extends QueryFactoryHolder implements GuildHolder {
    private Guild guild;

    public RepGuild(DataSource dataSource, Guild guild) {
        super(dataSource);
        this.guild = guild;
    }

    public Guild guild() {
        return guild;
    }

    public RepGuild refresh(Guild guild) {
        this.guild = guild;
        return this;
    }

    public List<Long> userIds() {
        return builder(Long.class)
                .query("""
                        SELECT
                        	user_id AS user_id
                        FROM
                        	(
                        		SELECT
                        			donor_id AS user_id
                        		FROM
                        			reputation_log
                        		WHERE guild_id = ?
                        		UNION
                        		DISTINCT
                        		SELECT
                        			receiver_id AS user_id
                        		FROM
                        			reputation_log
                        		WHERE guild_id = ?
                        	) users
                        WHERE user_id != 0
                         """)
                .paramsBuilder(stmt -> stmt.setLong(guildId()).setLong(guildId()))
                .readRow(rs -> rs.getLong("user_id"))
                .allSync();
    }

}
