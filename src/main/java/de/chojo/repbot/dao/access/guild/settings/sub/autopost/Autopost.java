/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub.autopost;

import com.fasterxml.jackson.annotation.JsonSerializeAs;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.web.pojo.settings.sub.AutopostPOJO;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.sql.SQLException;
import java.util.function.Function;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

@JsonSerializeAs(AutopostPOJO.class)
public class Autopost extends AutopostPOJO implements GuildHolder {
    private final Settings settings;

    private Autopost(
            Settings settings,
            boolean active,
            long channelId,
            long message,
            RefreshType refreshType,
            RefreshInterval refreshInterval) {
        this.settings = settings;
        this.active = active;
        this.channelId = channelId;
        this.messageId = message;
        this.refreshType = refreshType;
        this.refreshInterval = refreshInterval;
    }

    public Autopost(Settings settings) {
        this.settings = settings;
    }

    public static Autopost build(Settings settings, Row rs) throws SQLException {
        return new Autopost(
                settings,
                rs.getBoolean("active"),
                rs.getLong("channel_id"),
                rs.getLong("message_id"),
                rs.getEnum("refresh_type", RefreshType.class),
                rs.getEnum("refresh_interval", RefreshInterval.class));
    }

    public boolean active(boolean active) {
        if (set("active", stmt -> stmt.bind(active))) {
            this.active = active;
        }
        return this.active;
    }

    public long channel(TextChannel textChannel) {
        return channel(textChannel.getIdLong());
    }

    public long channel(long channelId) {
        if (set("channel_id", stmt -> stmt.bind(channelId))) {
            this.channelId = channelId;
        }
        return this.channelId;
    }

    public long message(Message message) {
        return message(message.getIdLong());
    }

    public long message(long messageId) {
        if (set("message_id", stmt -> stmt.bind(messageId))) {
            this.messageId = messageId;
        }
        return this.messageId;
    }

    public RefreshType refreshType(RefreshType refreshType) {
        if (set("refresh_type", stmt -> stmt.bind(refreshType))) {
            this.refreshType = refreshType;
        }
        return refreshType;
    }

    public RefreshInterval refreshInterval(RefreshInterval refreshInterval) {
        if (set("refresh_interval", stmt -> stmt.bind(refreshInterval))) {
            this.refreshInterval = refreshInterval;
        }
        return refreshInterval;
    }

    public void apply(AutopostPOJO state) {
        if (this.active != state.active()) active(state.active());
        if (this.channelId != state.channelId()) channel(state.channelId());
        if (this.messageId != state.messageId()) message(state.messageId());
        if (this.refreshType != state.refreshType()) refreshType(state.refreshType());
        if (this.refreshInterval != state.refreshInterval()) refreshInterval(state.refreshInterval());
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }

    @Override
    public long guildId() {
        return settings.guildId();
    }

    private boolean set(String parameter, Function<Call, Call> builder) {
        return query("""
                INSERT INTO autopost(guild_id, %s) VALUES (?, ?)
                ON CONFLICT(guild_id)
                    DO UPDATE SET %s = excluded.%s;
                """, parameter, parameter, parameter)
                .single(builder.apply(call().bind(guildId())))
                .insert()
                .changed();
    }
}
