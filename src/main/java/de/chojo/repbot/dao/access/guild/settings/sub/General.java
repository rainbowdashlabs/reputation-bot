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
import de.chojo.repbot.web.pojo.settings.sub.GeneralPOJO;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.time.Instant;
import java.util.function.Function;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

@JsonSerializeAs(GeneralPOJO.class)
public class General extends GeneralPOJO implements GuildHolder {
    private final Settings settings;

    public General(Settings settings) {
        this(settings, null, false, ReputationMode.TOTAL, null,0);
    }

    public General(Settings settings, DiscordLocale language, boolean stackRoles, ReputationMode reputationMode, Instant resetDate, long systemChannel) {
        super(stackRoles, language, reputationMode, resetDate, systemChannel);
        this.settings = settings;
    }

    public static General build(Settings settings, Row rs) throws SQLException {
        var lang = rs.getString("language");
        return new General(settings,
                lang == null ? null : DiscordLocale.from(lang),
                rs.getBoolean("stack_roles"),
                ReputationMode.valueOf(rs.getString("reputation_mode")),
                rs.get("reset_date", INSTANT_TIMESTAMP),
                rs.getLong("system_channel_id"));
    }

    public boolean language(@Nullable DiscordLocale language) {
        var result = set("language", stmt -> stmt.bind(language == null ? null : language.getLocale()));
        if (result) {
            this.language = language;
        }
        return result;
    }

    public boolean systemChannel(long channel) {
        var result = set("system_channel_id", stmt -> stmt.bind(channel));
        if (result) {
            this.systemChannel = channel;
        }
        return result;
    }

    public ReputationMode reputationMode(ReputationMode reputationMode) {
        var result = set("reputation_mode", stmt -> stmt.bind(reputationMode.name()));
        if (result) {
            this.reputationMode = reputationMode;
        }
        return reputationMode;
    }

    public boolean isStackRoles(boolean stackRoles) {
        var result = set("stack_roles", stmt -> stmt.bind(stackRoles));
        if (result) {
            this.stackRoles = stackRoles;
        }
        return result;
    }

    public boolean resetDate(Instant resetDate) {
        var result = set("reset_date", stmt -> stmt.bind(resetDate, INSTANT_TIMESTAMP));
        if (result) {
            this.resetDate = resetDate;
        }
        return result;
    }
    
    public boolean resetDateNow() {
        return resetDate(Instant.now());
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
                INSERT INTO guild_settings(guild_id, %s) VALUES (?, ?)
                ON CONFLICT(guild_id)
                    DO UPDATE SET %s = excluded.%s;
                """, parameter, parameter, parameter)
                .single(builder.apply(call().bind(guildId())))
                .insert()
                .changed();
    }

    public String prettyString() {
        return """
                Stack roles: %s
                Language: %s
                Reputation Mode: %s
                System Channel: %s
                """.stripIndent()
                   .formatted(stackRoles, language != null ? language.getLanguageName() : guild().getLocale().getLanguageName(), reputationMode.name(), systemChannel);
    }
}
