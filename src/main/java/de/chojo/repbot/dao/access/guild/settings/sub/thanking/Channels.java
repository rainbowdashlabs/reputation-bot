/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.components.GuildHolder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.internal.entities.channel.concrete.VoiceChannelImpl;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Class representing the channels configuration for thanking settings.
 */
public class Channels implements GuildHolder {

    private static final Logger log = getLogger(Channels.class);

    private final Thanking thanking;
    private final Set<Long> channels;
    private final Set<Long> categories;
    private boolean whitelist;

    /**
     * Constructs a Channels instance with the specified thanking settings, whitelist status, channels, and categories.
     *
     * @param thanking the thanking settings
     * @param whitelist the whitelist status
     * @param channels the set of channel IDs
     * @param categories the set of category IDs
     */
    public Channels(Thanking thanking, boolean whitelist, Set<Long> channels, Set<Long> categories) {
        this.thanking = thanking;
        this.whitelist = whitelist;
        this.channels = channels;
        this.categories = categories;
    }

    @Override
    public Guild guild() {
        return thanking.guild();
    }

    @Override
    public long guildId() {
        return thanking.guildId();
    }

    /**
     * Checks if the given channel is enabled for thanking.
     *
     * @param channel the guild message channel
     * @return true if the channel is enabled, false otherwise
     */
    public boolean isEnabled(GuildMessageChannel channel) {
        StandardGuildChannel baseChannel;
        if (channel instanceof ThreadChannel bc) {
            baseChannel = bc.getParentChannel().asStandardGuildChannel();
        } else {
            if (channel instanceof StandardGuildMessageChannel bc) {
                baseChannel = bc;
            } else if (channel instanceof VoiceChannelImpl bc) {
                baseChannel = bc.asStandardGuildChannel();
            } else {
                log.debug("Channel is a non base guild channel, but a {}.", channel.getClass().getName());
                return false;
            }
        }

        return isEnabledByChannel(baseChannel) || isEnabledByCategory(baseChannel.getParentCategory());
    }

    /**
     * Checks if the given channel is enabled by channel ID.
     *
     * @param channel the standard guild channel
     * @return true if the channel is enabled, false otherwise
     */
    public boolean isEnabledByChannel(StandardGuildChannel channel) {
        return isWhitelist() == channels.contains(channel.getIdLong());
    }

    /**
     * Checks if the given category is enabled by category ID.
     *
     * @param category the category
     * @return true if the category is enabled, false otherwise
     */
    public boolean isEnabledByCategory(@Nullable Category category) {
        if (category == null) return false;
        return isWhitelist() == categories.contains(category.getIdLong());
    }

    /**
     * Gets the list of enabled guild channels.
     *
     * @return the list of enabled guild channels
     */
    public List<GuildChannel> channels() {
        return channels.stream().map(guild()::getGuildChannelById)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Gets the list of enabled categories.
     *
     * @return the list of enabled categories
     */
    public List<Category> categories() {
        return categories.stream().map(guild()::getCategoryById)
                         .filter(Objects::nonNull)
                         .toList();
    }

    /**
     * Gets the set of channel IDs.
     *
     * @return the set of channel IDs
     */
    public Set<Long> channelIds() {
        return channels;
    }

    /**
     * Checks if the whitelist is enabled.
     *
     * @return true if the whitelist is enabled, false otherwise
     */
    public boolean isWhitelist() {
        return whitelist;
    }

    /**
     * Adds a channel to the reputation channels.
     *
     * @param channel the standard guild channel
     * @return true if the channel was added, false otherwise
     */
    public boolean add(StandardGuildChannel channel) {
        var result = query("INSERT INTO active_channel(guild_id, channel_id) VALUES(?,?) ON CONFLICT(guild_id, channel_id) DO NOTHING;")
                .single(call().bind(guildId()).bind(channel.getIdLong()))
                .update()
                .changed();
        if (result) {
            channels.add(channel.getIdLong());
        }
        return result;
    }

    /**
     * Adds a category to the reputation categories.
     *
     * @param category the category
     * @return true if the category was added, false otherwise
     */
    public boolean add(Category category) {
        var result = query("INSERT INTO active_categories(guild_id, category_id) VALUES(?,?) ON CONFLICT(guild_id, category_id) DO NOTHING;")
                .single(call().bind(guildId()).bind(category.getIdLong()))
                .update()
                .changed();
        if (result) {
            categories.add(category.getIdLong());
        }
        return result;
    }

    /**
     * Removes a reputation channel.
     *
     * @param channel the channel
     * @return true if the channel was removed, false otherwise
     */
    public boolean remove(Channel channel) {
        var result = query("DELETE FROM active_channel WHERE guild_id = ? AND channel_id = ?;")
                .single(call().bind(guildId()).bind(channel.getIdLong()))
                .update()
                .changed();
        if (result) {
            channels.remove(channel.getIdLong());
        }
        return result;
    }

    /**
     * Removes a reputation category.
     *
     * @param category the category
     * @return true if the category was removed, false otherwise
     */
    public boolean remove(Category category) {
        var result = query("DELETE FROM active_categories WHERE guild_id = ? AND category_id = ?;")
                .single(call().bind(guildId()).bind(category.getIdLong()))
                .update()
                .changed();
        if (result) {
            categories.remove(category.getIdLong());
        }
        return result;
    }

    /**
     * Removes all channels of a guild.
     *
     * @return the number of removed channels
     */
    public int clearChannel() {
        var result = query("DELETE FROM active_channel WHERE guild_id = ?;")
                .single(call().bind(guildId()))
                .update()
                .rows();
        if (result > 0) {
            channels.clear();
        }
        return result;
    }

    /**
     * Removes all categories of a guild.
     *
     * @return the number of removed categories
     */
    public int clearCategories() {
        var result = query("DELETE FROM active_categories WHERE guild_id = ?;")
                .single(call().bind(guildId()))
                .update()
                .rows();
        if (result > 0) {
            categories.clear();
        }
        return result;
    }

    /**
     * Sets the list type to whitelist or blacklist.
     *
     * @param whitelist the whitelist status
     * @return true if the whitelist status was updated, false otherwise
     */
    public boolean listType(boolean whitelist) {
        var result = query("""
                       INSERT INTO thank_settings(guild_id, channel_whitelist) VALUES (?,?)
                           ON CONFLICT(guild_id)
                               DO UPDATE
                                   SET channel_whitelist = excluded.channel_whitelist
                       """)
                .single(call().bind(guildId()).bind(whitelist))
                .update()
                .changed();
        if (result) {
            this.whitelist = whitelist;
        }
        return this.whitelist;
    }
}
