/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import com.fasterxml.jackson.annotation.JsonSerializeAs;
import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ChannelsPOJO;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static org.slf4j.LoggerFactory.getLogger;

@JsonSerializeAs(ChannelsPOJO.class)
public class Channels extends ChannelsPOJO implements GuildHolder {

    private static final Logger log = getLogger(Channels.class);

    private final Thanking thanking;

    public Channels(Thanking thanking, boolean whitelist, Set<Long> channels, Set<Long> categories) {
        super(channels, categories, whitelist);
        this.thanking = thanking;
    }

    @Override
    public Guild guild() {
        return thanking.guild();
    }

    @Override
    public long guildId() {
        return thanking.guildId();
    }

    public boolean isEnabled(GuildMessageChannel channel) {
        long baseChannel = channel.getIdLong();
        if (channel instanceof ThreadChannel bc) {
            baseChannel = bc.getParentChannel().getIdLong();
        }

        if (channel instanceof ICategorizableChannel categorizableChannel) {
            if (isEnabledByCategory(categorizableChannel.getParentCategory())) {
                return true;
            }
        }
        return isEnabledByChannel(baseChannel);
    }

    public boolean isEnabledByChannel(GuildMessageChannel channel) {
        return isEnabledByChannel(channel.getIdLong());
    }

    public boolean isEnabledByChannel(long channel) {
        return isWhitelist() == channels.contains(channel);
    }

    public boolean isEnabledByCategory(@Nullable Category category) {
        if (category == null) return false;
        return isWhitelist() == categories.contains(category.getIdLong());
    }

    public List<GuildChannel> channels() {
        return channels.stream().map(guild()::getGuildChannelById)
                       .filter(Objects::nonNull)
                       .toList();
    }

    public List<Category> categories() {
        return categories.stream().map(guild()::getCategoryById)
                         .filter(Objects::nonNull)
                         .toList();
    }

    /**
     * Add a channel to reputation channel
     *
     * @param channel channel
     * @return true if a channel was added
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
     * Add a category to reputation categories
     *
     * @param category category
     * @return true if a category was added
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
     * Remove a reputation channel
     *
     * @param channel channel
     * @return true if the channel was removed
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
     * Remove a reputation category
     *
     * @param category category
     * @return true if the channel was removed
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
     * Remove all channel of a guild
     *
     * @return the amount of removed channel
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
     * Remove all categories of a guild
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

    public boolean addChannel(long channelId) {
        var result = query("INSERT INTO active_channel(guild_id, channel_id) VALUES (?,?) ON CONFLICT(guild_id, channel_id) DO NOTHING")
                .single(call().bind(guildId()).bind(channelId))
                .update()
                .changed();
        if (result) {
            channels.add(channelId);
        }
        return this.whitelist;
    }

    public boolean addCategory(long categoryId) {
        var result = query("INSERT INTO active_categories(guild_id, category_id) VALUES (?,?) ON CONFLICT(guild_id, category_id) DO NOTHING")
                .single(call().bind(guildId()).bind(categoryId))
                .update()
                .changed();
        if (result) {
            categories.add(categoryId);
        }
        return this.whitelist;
    }

    public boolean removeChannel(long channelId) {
        var result = query("DELETE FROM active_channel WHERE guild_id = ? AND channel_id = ?")
                .single(call().bind(guildId()).bind(channelId))
                .update()
                .changed();
        if (result) {
            channels.remove(channelId);
        }
        return this.whitelist;
    }

    public boolean removeCategory(long categoryId) {
        var result = query("DELETE FROM active_categories WHERE guild_id = ? AND category_id = ?")
                .single(call().bind(guildId()).bind(categoryId))
                .update()
                .changed();
        if (result) {
            categories.remove(categoryId);
        }
        return this.whitelist;
    }

    public void apply(ChannelsPOJO state) {
        listType(state.isWhitelist());

        for (Long channelId : state.channelIds()) {
            if (!channels.contains(channelId)) {
                addChannel(channelId);
            }
        }
        for (Long channelId : Set.copyOf(channels)) {
            if (!state.channelIds().contains(channelId)) {
                removeChannel(channelId);
            }
        }

        for (Long categoryId : state.categoryIds()) {
            if (!categories.contains(categoryId)) {
                addCategory(categoryId);
            }
        }
        for (Long categoryId : Set.copyOf(categories)) {
            if (!state.categoryIds().contains(categoryId)) {
                removeCategory(categoryId);
            }
        }
    }
}
