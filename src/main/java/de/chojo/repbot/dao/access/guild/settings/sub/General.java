/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class General implements GuildHolder {
    private final AtomicBoolean stackRoles;
    private final Settings settings;
    private DiscordLocale language;
    private ReputationMode reputationMode;
    private LocalDate resetDate;
    private long systemChannel;

    public General(Settings settings) {
        this(settings, null, false, ReputationMode.TOTAL, null,0);
    }

    public General(Settings settings, DiscordLocale language, boolean stackRoles, ReputationMode reputationMode, LocalDate resetDate, long systemChannel) {
        this.settings = settings;
        this.language = language;
        this.stackRoles = new AtomicBoolean(stackRoles);
        this.reputationMode = reputationMode;
        this.resetDate = resetDate;
        this.systemChannel = systemChannel;
    }

    public static General build(Settings settings, Row rs) throws SQLException {
        var lang = rs.getString("language");
        return new General(settings,
                lang == null ? null : DiscordLocale.from(lang),
                rs.getBoolean("stack_roles"),
                ReputationMode.valueOf(rs.getString("reputation_mode")),
                Optional.ofNullable(rs.getTimestamp("reset_date")).map(ts -> ts.toInstant().atZone(ZoneOffset.UTC).toLocalDate()).orElse(null),
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

    public boolean stackRoles(boolean stackRoles) {
        var result = set("stack_roles", stmt -> stmt.bind(stackRoles));
        if (result) {
            this.stackRoles.set(stackRoles);
        }
        return result;
    }

    public boolean resetDate(LocalDate resetDate) {
        var utcTimestamp = resetDate == null ? null : Timestamp.from(resetDate.atStartOfDay(ZoneOffset.UTC).toInstant());
        var result = set("reset_date", stmt -> stmt.bind(utcTimestamp));
        if (result) {
            this.resetDate = resetDate;
        }
        return result;
    }
    
    public boolean resetDateNow() {
        var nowUtc = Timestamp.from(Instant.now());
        var result = set("reset_date", stmt -> stmt.bind(nowUtc));
        if (result) {
            this.resetDate = LocalDate.now(ZoneOffset.UTC);
        }
        return result;
    }

    public Optional<DiscordLocale> language() {
        return Optional.ofNullable(language);
    }

    public boolean isStackRoles() {
        return stackRoles.get();
    }

    public AtomicBoolean stackRoles() {
        return stackRoles;
    }

    public long systemChannel() {
        return systemChannel;
    }

    public LocalDate resetDate() {
        return resetDate;
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

    public ReputationMode reputationMode() {
        return reputationMode;
    }

    public String prettyString() {
        return """
                Stack roles: %s
                Language: %s
                Reputation Mode: %s
                System Channel: %s
                """.stripIndent()
                   .formatted(stackRoles.get(), language != null ? language.getLanguageName() : guild().getLocale().getLanguageName(), reputationMode.name(), systemChannel);
    }
}
