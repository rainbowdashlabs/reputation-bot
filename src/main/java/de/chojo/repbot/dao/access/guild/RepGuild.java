/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.reputation.Reputation;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class RepGuild implements GuildHolder {
    private static final Cache<Long, Cleanup> CLEANUPS = CacheBuilder.newBuilder()
                                                                     .expireAfterAccess(2, TimeUnit.MINUTES).build();
    private static final Cache<Long, Gdpr> GDPR = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.MINUTES)
                                                              .build();
    private final Reputation reputation;
    private final Subscriptions subscriptions;
    private final Settings settings;
    private Guild guild;
    private final Configuration configuration;

    public RepGuild(Guild guild, Configuration configuration) {
        super();
        this.configuration = configuration;
        subscriptions = new Subscriptions(this);
        reputation = new Reputation(this);
        settings = new Settings(this);
        this.guild = guild;
    }

    public Guild guild() {
        return guild;
    }

    @Override
    public long guildId() {
        return guild.getIdLong();
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

    //Todo: We don't actually know how many users are on a guild and saved in the database. At some point we might want to add some pagination here.

    /**
     * A list of user ids of all users which are connected to this guild.
     *
     * @return list of user ids
     */
    public List<Long> userIds() {
        return query("""
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
                .single(call().bind(guildId()).bind(guildId()))
                .mapAs(Long.class)
                .all();
    }

    public Reputation reputation() {
        return reputation;
    }

    public Settings settings() {
        return settings;
    }

    public Subscriptions subscriptions() {
        return subscriptions;
    }

    public Cleanup cleanup() {
        try {
            return CLEANUPS.get(guildId(), () -> new Cleanup(this));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isById() {
        return guild == null;
    }

    @Override
    public String toString() {
        return "RepGuild{" +
                "guild=" + guild +
                '}';
    }

    public RepGuild load(ShardManager shardManager) {
        if (guild != null) return this;
        guild = shardManager.getGuildById(guildId());
        return this;
    }

    public Configuration configuration() {
        return configuration;
    }
}
