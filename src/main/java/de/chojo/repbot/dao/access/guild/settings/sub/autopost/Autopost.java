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

    private Autopost(Settings settings, boolean active, long channelId, long message, RefreshType refreshType, RefreshInterval refreshInterval) {
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
        return new Autopost(settings,
                rs.getBoolean("active"),
                rs.getLong("channel_id"),
                rs.getLong("message_id"),
                rs.getEnum("refresh_type",RefreshType.class),
                rs.getEnum("refresh_interval", RefreshInterval.class));
    }

    public boolean active(boolean active) {
        if (set("active", stmt -> stmt.bind(active))) {
            this.active = active;
        }
        return this.active;
    }

    public long channel(TextChannel textChannel) {
        if (set("channel_id", stmt -> stmt.bind(textChannel.getIdLong()))) {
            this.channelId = textChannel.getIdLong();
        }
        return channelId;
    }

    public long message(Message message) {
        if (set("message_id", stmt -> stmt.bind(message.getIdLong()))) {
            this.messageId = message.getIdLong();
        }
        return message.getIdLong();
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

    @Override
    public Guild guild() {
        return settings.guild();
    }

    @Override
    public long guildId() {
        return settings.guildId();
    }
}
