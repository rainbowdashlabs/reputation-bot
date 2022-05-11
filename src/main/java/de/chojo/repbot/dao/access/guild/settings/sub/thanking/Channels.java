package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.ThreadChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Channels extends QueryFactoryHolder implements GuildHolder {
    private final Thanking thanking;
    private final Set<Long> channels;
    private boolean whitelist;

    public Channels(Thanking thanking, boolean whitelist, Set<Long> channels) {
        super(thanking);
        this.thanking = thanking;
        this.whitelist = whitelist;
        this.channels = channels;
    }

    @Override
    public Guild guild() {
        return thanking.guild();
    }

    public boolean isEnabled(Channel channel) {
        if (channel.getType() == ChannelType.GUILD_PUBLIC_THREAD) {
            channel = ((ThreadChannel) channel).getParentMessageChannel();
        }

        if (isWhitelist()) {
            return channels.contains(channel.getIdLong());
        }
        return !channels.contains(channel.getIdLong());
    }

    public List<TextChannel> channels() {
        return DiscordResolver.getValidTextChannelsById(guild(), new ArrayList<>(channels));
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
    public boolean add(MessageChannel channel) {
        var result = builder()
                             .query("INSERT INTO active_channel(guild_id, channel_id) VALUES(?,?) ON CONFLICT(guild_id, channel_id) DO NOTHING;")
                             .paramsBuilder(stmt -> stmt.setLong(guildId()).setLong(channel.getIdLong()))
                             .update()
                             .executeSync() > 0;
        if (result) {
            channels.add(channel.getIdLong());
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
                             .paramsBuilder(stmt -> stmt.setLong(guildId()).setLong(channel.getIdLong()))
                             .update()
                             .executeSync() > 0;
        if (result) {
            channels.remove(channel.getIdLong());
        }
        return result;
    }

    /**
     * Remove all channel of a guild
     *
     * @return the amount of removed channel
     */
    public int clear() {
        var result = builder()
                .query("DELETE FROM active_channel WHERE guild_id = ?;")
                .paramsBuilder(stmt -> stmt.setLong(guildId()))
                .update()
                .executeSync();
        if (result > 0) {
            channels.clear();
        }
        return result;
    }

    public boolean listType(boolean whitelist) {
        var result = builder().query("""
                        INSERT INTO thank_settings(guild_id, channel_whitelist) VALUES (?,?)
                            ON CONFLICT(guild_id)
                                DO UPDATE
                                    SET channel_whitelist = excluded.channel_whitelist
                        """)
                                 .paramsBuilder(stmt -> stmt.setLong(guildId()).setBoolean(whitelist))
                                 .update()
                                 .executeSync() > 0;
        if (result) {
            this.whitelist = whitelist;
        }
        return this.whitelist;
    }
}
