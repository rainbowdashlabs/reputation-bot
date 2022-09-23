package de.chojo.repbot.dao.provider;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.RepGuildIdImpl;
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

    public Guilds(DataSource dataSource) {
        super(dataSource);
    }

    public RepGuild guild(Guild guild) {
        try {
            return guilds.get(guild.getIdLong(), () -> new RepGuild(source(), guild)).refresh(guild);
        } catch (ExecutionException e) {
            log.error("Could not create guild adapter", e);
            throw new RuntimeException("", e);
        }
    }

    public RepGuild guild(long id) {
        return new RepGuildIdImpl(source(), id);
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
                .readRow(row -> new RepGuildIdImpl(source(), row.getLong("guild_id")))
                .allSync();
    }
}
