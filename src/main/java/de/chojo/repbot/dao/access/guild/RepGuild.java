package de.chojo.repbot.dao.access.guild;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.repbot.dao.access.guild.reputation.Reputation;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import net.dv8tion.jda.api.entities.Guild;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RepGuild extends QueryFactoryHolder implements GuildHolder {
    private static final Cache<Long, Cleanup> CLEANUPS = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.MINUTES).build();
    private static final Cache<Long, Migration> MIGRATIONS = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.MINUTES).build();
    private static final Cache<Long, Gdpr> GDPR = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.MINUTES).build();
    private Guild guild;
    private final Reputation reputation;
    private final Settings settings;

    public RepGuild(DataSource dataSource, Guild guild) {
        super(dataSource);
        reputation = new Reputation(this);
        settings = new Settings(this);
        this.guild = guild;
    }

    public Guild guild() {
        return guild;
    }

    public Gdpr gdpr() {
        try {
            return GDPR.get(guildId(), () -> new Gdpr(this));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public RepGuild refresh(Guild guild) {
        this.guild = guild;
        return this;
    }

    //Todo: We dont actually know how many users are on a guild and saved in the database. At some point we might want to add some pagination here.

    /**
     * A list of user ids of all users which are connected to this guild.
     *
     * @return list of user ids
     */
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

    public Reputation reputation() {
        return reputation;
    }

    public Migration migration() {
        try {
            return MIGRATIONS.get(guildId(), () -> new Migration(this));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Settings settings() {
        return settings;
    }

    public Cleanup cleanup() {
        try {
            return CLEANUPS.get(guildId(), () -> new Cleanup(this));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "RepGuild{" +
               "guild=" + guild +
               '}';
    }
}
