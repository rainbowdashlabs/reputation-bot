/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.provider;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.RepGuildId;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.pagination.GuildList;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static org.slf4j.LoggerFactory.getLogger;

public class Guilds {
    private static final Logger log = getLogger(Guilds.class);
    private final Cache<Long, RepGuild> guilds = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES)
                                                             .build();
    private final Configuration configuration;

    public Guilds(Configuration configuration) {
        this.configuration = configuration;
    }

    public RepGuild guild(Guild guild) {
        try {
            return guilds.get(guild.getIdLong(), () -> new RepGuild(guild, configuration)).refresh(guild);
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
    public RepGuild byId(long id) {
        var cached = guilds.getIfPresent(id);
        return cached != null ? cached : new RepGuildId(id, configuration);
    }

    public List<RepGuild> byReputationMode(ReputationMode mode) {
        return query("SELECT guild_id FROM guild_settings WHERE reputation_mode = ?")
                .single(call().bind(mode.name()))
                .map(row -> byId(row.getLong("guild_id")))
                .all();
    }

    public GuildList guilds(int pageSize) {
        return new GuildList(() -> pages(pageSize), page -> page(pageSize, page));
    }

    private Integer pages(int pageSize) {
        return query("""
                       SELECT
                           CEIL(COUNT(1)::numeric / ?)::INTEGER AS count
                       FROM
                           guilds
                       """)
                .single(call().bind(pageSize))
                .mapAs(Integer.class)
                .first()
                .orElse(1);
    }

    private List<RepGuild> page(int pageSize, int page) {
        return query("""
                       SELECT guild_id FROM guilds
                       OFFSET ?
                       LIMIT ?;
                       """)
                .single(call().bind(page * pageSize).bind(pageSize))
                .map(row -> byId(row.getLong("guild_id")))
                .all();
    }

    public void invalidate(long guild) {
        guilds.invalidate(guild);
    }
}
