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

/**
 * Represents a guild with reputation and settings management.
 */
public class RepGuild implements GuildHolder {
    private static final Cache<Long, Cleanup> CLEANUPS = CacheBuilder.newBuilder()
                                                                     .expireAfterAccess(2, TimeUnit.MINUTES).build();
    private static final Cache<Long, Gdpr> GDPR = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.MINUTES)
                                                              .build();
    private final Reputation reputation;
    private final Settings settings;
    private Guild guild;
    private final Configuration configuration;

    /**
     * Constructs a RepGuild with the specified guild and configuration.
     *
     * @param guild the guild
     * @param configuration the configuration
     */
    public RepGuild(Guild guild, Configuration configuration) {
        super();
        this.configuration = configuration;
        reputation = new Reputation(this);
        settings = new Settings(this);
        this.guild = guild;
    }

    /**
     * Returns the guild.
     *
     * @return the guild
     */
    public Guild guild() {
        return guild;
    }

    /**
     * Returns the guild ID.
     *
     * @return the guild ID
     */
    @Override
    public long guildId() {
        return guild.getIdLong();
    }

    /**
     * Returns the GDPR settings for the guild.
     *
     * @return the GDPR settings
     */
    public Gdpr gdpr() {
        try {
            return GDPR.get(guildId(), () -> new Gdpr(this));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Refreshes the guild with the specified guild.
     *
     * @param guild the guild
     * @return the refreshed RepGuild
     */
    public RepGuild refresh(Guild guild) {
        this.guild = guild;
        return this;
    }

    /**
     * Returns a list of user IDs of all users connected to this guild.
     *
     * @return list of user IDs
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

    /**
     * Returns the reputation management for the guild.
     *
     * @return the reputation management
     */
    public Reputation reputation() {
        return reputation;
    }

    /**
     * Returns the settings for the guild.
     *
     * @return the settings
     */
    public Settings settings() {
        return settings;
    }

    /**
     * Returns the cleanup settings for the guild.
     *
     * @return the cleanup settings
     */
    public Cleanup cleanup() {
        try {
            return CLEANUPS.get(guildId(), () -> new Cleanup(this));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if the guild is identified by ID.
     *
     * @return true if the guild is identified by ID, false otherwise
     */
    public boolean isById() {
        return guild == null;
    }

    /**
     * Returns a string representation of the RepGuild.
     *
     * @return a string representation of the RepGuild
     */
    @Override
    public String toString() {
        return "RepGuild{" +
                "guild=" + guild +
                '}';
    }

    /**
     * Loads the guild using the specified shard manager.
     *
     * @param shardManager the shard manager
     * @return the loaded RepGuild
     */
    public RepGuild load(ShardManager shardManager) {
        if (guild != null) return this;
        guild = shardManager.getGuildById(guildId());
        return this;
    }

    /**
     * Returns the configuration for the guild.
     *
     * @return the configuration
     */
    public Configuration configuration() {
        return configuration;
    }
}
