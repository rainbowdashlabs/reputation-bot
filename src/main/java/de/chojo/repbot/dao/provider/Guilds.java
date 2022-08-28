package de.chojo.repbot.dao.provider;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.repbot.dao.access.guild.RepGuild;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class Guilds {
    private static final Logger log = getLogger(Guilds.class);
    private final DataSource dataSource;
    private final Cache<Long, RepGuild> guilds = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES)
                                                             .build();

    public Guilds(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public RepGuild guild(Guild guild) {
        try {
            return guilds.get(guild.getIdLong(), () -> new RepGuild(dataSource, guild)).refresh(guild);
        } catch (ExecutionException e) {
            log.error("Could not create guild adapter", e);
            throw new RuntimeException("", e);
        }
    }
}
