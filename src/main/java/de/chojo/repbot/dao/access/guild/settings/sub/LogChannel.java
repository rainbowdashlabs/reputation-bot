/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

import com.fasterxml.jackson.annotation.JsonSerializeAs;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.web.pojo.settings.sub.AnnouncementsPOJO;
import de.chojo.repbot.web.pojo.settings.sub.LogChannelPOJO;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.sql.SQLException;
import java.util.function.Function;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

@JsonSerializeAs(LogChannelPOJO.class)
public class LogChannel extends LogChannelPOJO implements GuildHolder {
    private final Settings settings;

    public LogChannel(Settings settings) {
        super();
        this.settings = settings;
    }

    public LogChannel(Settings settings, long channelId, boolean active) {
        super(channelId, active);
        this.settings = settings;
    }

    public static LogChannel build(Settings settings, Row row) throws SQLException {
        return new LogChannel(settings,
                row.getLong("channel_id"),
                row.getBoolean("active")
        );
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
                INSERT INTO log_channel(guild_id, %s) VALUES (?, ?)
                ON CONFLICT(guild_id)
                    DO UPDATE SET %s = excluded.%s;
                """, parameter, parameter, parameter)
                .single(builder.apply(call().bind(guildId())))
                .insert()
                .changed();
    }
}
