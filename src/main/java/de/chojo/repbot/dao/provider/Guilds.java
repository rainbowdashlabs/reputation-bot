package de.chojo.repbot.dao.provider;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.RepGuildId;
import de.chojo.repbot.dao.pagination.GuildList;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class Guilds extends QueryFactory {
    private static final Logger log = getLogger(Guilds.class);
    private final Cache<Long, RepGuild> guilds = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES)
                                                             .build();
    private final Configuration configuration;

    public Guilds(DataSource dataSource, Configuration configuration) {
        super(dataSource);
        this.configuration = configuration;
    }

    public RepGuild guild(Guild guild) {
        try {
            return guilds.get(guild.getIdLong(), () -> new RepGuild(source(), guild, configuration)).refresh(guild);
        } catch (ExecutionException e) {
            log.error("Could not create guild adapter", e);
            throw new RuntimeException("", e);
        }
    }

    /**
     * Gets a guild by id. This guild object might have limited functionality. This object is never cached.
     * It should never be used to change settings.
     * <p>
     * There is no gurantee that this guild will have any data stored in the database.
     *
     * @param id id of guild to create.
     * @return repguild created based on an id
     */
    public RepGuild guild(long id) {
        return new RepGuildId(source(), id, configuration);
    }

    public GuildList guilds(int pageSize) {
        return new GuildList(() -> pages(pageSize), page -> page(pageSize, page));
    }

    private Integer pages(int pageSize) {
        return builder(Integer.class)
                .query("""
                       SELECT
                           CEIL(COUNT(1)::numeric / ?) AS count
                       FROM
                           guilds
                       """)
                .parameter(stmt -> stmt.setInt(pageSize))
                .readRow(row -> row.getInt("count"))
                .firstSync()
                .orElse(1);
    }

    private List<RepGuild> page(int pageSize, int page) {
        return builder(RepGuild.class)
                .query("""
                       SELECT guild_id FROM guilds
                       OFFSET ?
                       LIMIT ?;
                       """)
                .parameter(stmt -> stmt.setInt(page * pageSize).setInt(pageSize))
                .readRow(row -> new RepGuildId(source(), row.getLong("guild_id"), configuration))
                .allSync();
    }
}
