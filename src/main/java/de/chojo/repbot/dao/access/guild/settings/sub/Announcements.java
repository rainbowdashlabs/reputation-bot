/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.sql.SQLException;
import java.util.function.Function;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

/**
 * Handles announcements settings for a guild.
 */
public class Announcements implements GuildHolder {
    private final Settings settings;
    private boolean active = false;
    private boolean sameChannel = true;
    private long channelId = 0;

    /**
     * Constructs an Announcements object with specified settings.
     *
     * @param settings the settings
     * @param active whether the announcements are active
     * @param sameChannel whether the announcements should be sent to the same channel
     * @param channelId the ID of the channel for announcements
     */
    private Announcements(Settings settings, boolean active, boolean sameChannel, long channelId) {
        this.settings = settings;
        this.active = active;
        this.sameChannel = sameChannel;
        this.channelId = channelId;
    }

    /**
     * Constructs an Announcements object with specified settings.
     *
     * @param settings the settings
     */
    public Announcements(Settings settings) {
        this.settings = settings;
    }

    /**
     * Builds an Announcements object from the database row.
     *
     * @param settings the settings
     * @param rs the database row
     * @return the Announcements object
     * @throws SQLException if a database access error occurs
     */
    public static Announcements build(Settings settings, Row rs) throws SQLException {
        return new Announcements(settings,
                rs.getBoolean("active"),
                rs.getBoolean("same_channel"),
                rs.getLong("channel_id"));
    }

    /**
     * Checks if the announcements are active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Checks if the announcements should be sent to the same channel.
     *
     * @return true if same channel, false otherwise
     */
    public boolean isSameChannel() {
        return sameChannel;
    }

    /**
     * Gets the ID of the channel for announcements.
     *
     * @return the channel ID
     */
    public long channelId() {
        return channelId;
    }

    /**
     * Sets the active status of the announcements.
     *
     * @param active the active status
     * @return the updated active status
     */
    public boolean active(boolean active) {
        if (set("active", stmt -> stmt.bind(active))) {
            this.active = active;
        }
        return this.active;
    }

    /**
     * Sets whether the announcements should be sent to the same channel.
     *
     * @param sameChannel the same channel status
     * @return the updated same channel status
     */
    public boolean sameChannel(boolean sameChannel) {
        if (set("same_channel", stmt -> stmt.bind(sameChannel))) {
            this.sameChannel = sameChannel;
        }
        return this.sameChannel;
    }

    /**
     * Sets the channel for announcements.
     *
     * @param textChannel the text channel
     * @return the updated channel ID
     */
    public long channel(TextChannel textChannel) {
        if (set("channel_id", stmt -> stmt.bind(textChannel.getIdLong()))) {
            channelId = textChannel.getIdLong();
        }
        return channelId;
    }

    /**
     * Sets a parameter in the database.
     *
     * @param parameter the parameter to set
     * @param builder the function to build the call
     * @return true if the parameter was set successfully, false otherwise
     */
    private boolean set(String parameter, Function<Call, Call> builder) {
        return query("""
                       INSERT INTO announcements(guild_id, %s) VALUES (?, ?)
                       ON CONFLICT(guild_id)
                           DO UPDATE SET %s = excluded.%s;
                       """, parameter, parameter, parameter)
                .single(builder.apply(call().bind(guildId())))
                .insert()
                .changed();
    }

    /**
     * Gets the guild associated with the settings.
     *
     * @return the guild
     */
    @Override
    public Guild guild() {
        return settings.guild();
    }

    /**
     * Gets the ID of the guild associated with the settings.
     *
     * @return the guild ID
     */
    @Override
    public long guildId() {
        return settings.guildId();
    }

    /**
     * Returns a pretty string representation of the announcement settings.
     *
     * @return the pretty string
     */
    public String prettyString() {
        return """
               Active: %s
               Same channel: %s
               Channel: %s
               """.stripIndent()
                .formatted(active, sameChannel, channelId);
    }
}
