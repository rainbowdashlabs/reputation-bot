package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.ThreadChannel;

import java.util.Set;

public class Channels extends QueryFactoryHolder implements GuildHolder {
    private final Thanking thanking;
    private final Set<Long> channels;
    private boolean channelWhitelist;

    public Channels(Thanking thanking, boolean channelWhitelist, Set<Long> channels) {
        super(thanking);
        this.thanking = thanking;
        this.channelWhitelist = channelWhitelist;
        this.channels = channels;
    }

    @Override
    public Guild guild() {
        return thanking.guild();
    }

    public boolean isReputationChannel(Channel channel) {
        if (channel.getType() == ChannelType.GUILD_PUBLIC_THREAD) {
            channel = ((ThreadChannel) channel).getParentMessageChannel();
        }

        if (isChannelWhitelist()) {
            return channels.contains(channel.getIdLong());
        }
        return !channels.contains(channel.getIdLong());
    }

    public boolean isChannelWhitelist() {
        return channelWhitelist;
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
     * @param guild guild
     * @return the amount of removed channel
     */
    public int clear(Guild guild) {
        var result = builder()
                .query("DELETE FROM active_channel WHERE guild_id = ?;")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .update()
                .executeSync();
        if (result > 0) {
            channels.clear();
        }
        return result;
    }

    public void listType(boolean whitelist) {
        builder().query("""
                        INSERT INTO thank_settings(guild_id, channel_whitelist) VALUES (?,?)
                            ON CONFLICT(guild_id)
                                DO UPDATE
                                    SET channel_whitelist = excluded.channel_whitelist
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guildId()).setBoolean(whitelist))
                .update()
                .executeSync();
    }
}
