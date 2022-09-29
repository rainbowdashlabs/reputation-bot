package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class Channels extends QueryFactory implements GuildHolder {

    private static final Logger log = getLogger(Channels.class);

    private final Thanking thanking;
    private final Set<Long> channels;
    private final Set<Long> categories;
    private boolean whitelist;

    public Channels(Thanking thanking, boolean whitelist, Set<Long> channels, Set<Long> categories) {
        super(thanking);
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

    public boolean isEnabled(GuildMessageChannel channel) {
        StandardGuildChannel baseChannel;
        if (channel instanceof ThreadChannel bc) {
            baseChannel = bc.getParentChannel().asStandardGuildChannel();
        } else {
            if (channel instanceof StandardGuildMessageChannel bc) {
                baseChannel = bc;
            } else {
                log.debug("Channel is a non base guild channel, but a {}.", channel.getClass().getName());
                return false;
            }
        }

        return isEnabledByChannel(baseChannel) || isEnabledByCategory(baseChannel.getParentCategory());
    }

    public boolean isEnabledByChannel(StandardGuildChannel channel) {
        if (channels.isEmpty()) return false;
        if (isWhitelist()) {
            return channels.contains(channel.getIdLong());
        }
        return !channels.contains(channel.getIdLong());
    }

    public boolean isEnabledByCategory(@Nullable Category category) {
        if (category == null) return false;
        if (categories.isEmpty()) return false;
        if (isWhitelist()) {
            return categories.contains(category.getIdLong());
        }
        return !categories.contains(category.getIdLong());
    }

    public List<TextChannel> channels() {
        return DiscordResolver.getValidTextChannelsById(guild(), new ArrayList<>(channels));
    }

    public List<Category> categories() {
        return categories.stream().map(guild()::getCategoryById)
                         .filter(Objects::nonNull)
                         .collect(Collectors.toList());
    }

    public Set<Long> channelIds() {
        return channels;
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    /**
     * Add a channel to reputation channel
     *
     * @param channel channel
     * @return true if a channel was added
     */
    public boolean add(StandardGuildChannel channel) {
        var result = builder()
                .query("INSERT INTO active_channel(guild_id, channel_id) VALUES(?,?) ON CONFLICT(guild_id, channel_id) DO NOTHING;")
                .parameter(stmt -> stmt.setLong(guildId()).setLong(channel.getIdLong()))
                .update()
                .sendSync()
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
        var result = builder()
                .query("INSERT INTO active_categories(guild_id, category_id) VALUES(?,?) ON CONFLICT(guild_id, category_id) DO NOTHING;")
                .parameter(stmt -> stmt.setLong(guildId()).setLong(category.getIdLong()))
                .update()
                .sendSync()
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
        var result = builder()
                .query("DELETE FROM active_channel WHERE guild_id = ? AND channel_id = ?;")
                .parameter(stmt -> stmt.setLong(guildId()).setLong(channel.getIdLong()))
                .update()
                .sendSync()
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
        var result = builder()
                .query("DELETE FROM active_categories WHERE guild_id = ? AND category_id = ?;")
                .parameter(stmt -> stmt.setLong(guildId()).setLong(category.getIdLong()))
                .update()
                .sendSync()
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
        var result = builder()
                .query("DELETE FROM active_channel WHERE guild_id = ?;")
                .parameter(stmt -> stmt.setLong(guildId()))
                .update()
                .sendSync()
                .rows();
        if (result > 0) {
            channels.clear();
        }
        return result;
    }

    /**
     * Remove all categories of a guild
     *
     * @return the amount of removed categories
     */
    public int clearCategories() {
        var result = builder()
                .query("DELETE FROM active_categories WHERE guild_id = ?;")
                .parameter(stmt -> stmt.setLong(guildId()))
                .update()
                .sendSync()
                .rows();
        if (result > 0) {
            categories.clear();
        }
        return result;
    }

    public boolean listType(boolean whitelist) {
        var result = builder()
                .query("""
                       INSERT INTO thank_settings(guild_id, channel_whitelist) VALUES (?,?)
                           ON CONFLICT(guild_id)
                               DO UPDATE
                                   SET channel_whitelist = excluded.channel_whitelist
                       """)
                .parameter(stmt -> stmt.setLong(guildId()).setBoolean(whitelist))
                .update()
                .sendSync()
                .changed();
        if (result) {
            this.whitelist = whitelist;
        }
        return this.whitelist;
    }
}
