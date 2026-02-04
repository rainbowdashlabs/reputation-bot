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
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.sql.SQLException;
import java.util.function.Function;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

@JsonSerializeAs(AnnouncementsPOJO.class)
public class Announcements extends AnnouncementsPOJO implements GuildHolder {
    private final Settings settings;

    private Announcements(Settings settings, boolean active, boolean sameChannel, long channelId) {
        this.settings = settings;
        this.active = active;
        this.sameChannel = sameChannel;
        this.channelId = channelId;
    }

    public Announcements(Settings settings) {
        this.settings = settings;
    }

    public static Announcements build(Settings settings, Row rs) throws SQLException {
        return new Announcements(settings,
                rs.getBoolean("active"),
                rs.getBoolean("same_channel"),
                rs.getLong("channel_id"));
    }

    public boolean active(boolean active) {
        if (set("active", stmt -> stmt.bind(active))) {
            this.active = active;
        }
        return this.active;
    }

    public boolean sameChannel(boolean sameChannel) {
        if (set("same_channel", stmt -> stmt.bind(sameChannel))) {
            this.sameChannel = sameChannel;
        }
        return this.sameChannel;
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

    public void apply(AnnouncementsPOJO state) {
        if (active != state.isActive()) active(state.isActive());
        if (sameChannel != state.isSameChannel()) sameChannel(state.isSameChannel());
        if (channelId != state.channelId()) channel(state.channelId());
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }

    @Override
    public long guildId() {
        return settings.guildId();
    }

    public String prettyString() {
        return """
                Active: %s
                Same channel: %s
                Channel: %s
                """.stripIndent()
                   .formatted(active, sameChannel, channelId);
    }

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
}
