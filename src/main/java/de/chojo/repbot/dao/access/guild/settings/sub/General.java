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

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

/**
 * Represents the general settings for a guild.
 */
public class General implements GuildHolder {
    private final AtomicBoolean stackRoles;
    private final Settings settings;
    private DiscordLocale language;
    private boolean emojiDebug;
    private ReputationMode reputationMode;
    private LocalDate resetDate;

    /**
     * Constructs a new General instance with default values.
     *
     * @param settings the Settings instance
     */
    public General(Settings settings) {
        this(settings, null, true, false, ReputationMode.TOTAL, null);
    }

    /**
     * Constructs a new General instance with specified values.
     *
     * @param settings the Settings instance
     * @param language the DiscordLocale for the language
     * @param emojiDebug whether emoji debug is enabled
     * @param stackRoles whether stack roles is enabled
     * @param reputationMode the ReputationMode
     * @param resetDate the reset date
     */
    public General(Settings settings, DiscordLocale language, boolean emojiDebug, boolean stackRoles, ReputationMode reputationMode, LocalDate resetDate) {
        this.settings = settings;
        this.language = language;
        this.emojiDebug = emojiDebug;
        this.stackRoles = new AtomicBoolean(stackRoles);
        this.reputationMode = reputationMode;
        this.resetDate = resetDate;
    }

    /**
     * Builds a General instance from the given database row.
     *
     * @param settings the Settings instance
     * @param rs the database row
     * @return the General instance
     * @throws SQLException if a database access error occurs
     */
    public static General build(Settings settings, Row rs) throws SQLException {
        var lang = rs.getString("language");
        return new General(settings,
                lang == null ? null : DiscordLocale.from(lang),
                rs.getBoolean("emoji_debug"),
                rs.getBoolean("stack_roles"),
                ReputationMode.valueOf(rs.getString("reputation_mode")),
                Optional.ofNullable(rs.getDate("reset_date")).map(Date::toLocalDate).orElse(null));
    }

    /**
     * Sets the language for the guild.
     *
     * @param language the DiscordLocale for the language
     * @return true if the language was successfully set, false otherwise
     */
    public boolean language(@Nullable DiscordLocale language) {
        var result = set("language", stmt -> stmt.bind(language == null ? null : language.getLocale()));
        if (result) {
            this.language = language;
        }
        return result;
    }

    /**
     * Sets the emoji debug status for the guild.
     *
     * @param emojiDebug whether emoji debug is enabled
     * @return true if the emoji debug status was successfully set, false otherwise
     */
    public boolean emojiDebug(boolean emojiDebug) {
        var result = set("emoji_debug", stmt -> stmt.bind(emojiDebug));
        if (result) {
            this.emojiDebug = emojiDebug;
        }
        return result;
    }

    /**
     * Sets the reputation mode for the guild.
     *
     * @param reputationMode the ReputationMode
     * @return the ReputationMode that was set
     */
    public ReputationMode reputationMode(ReputationMode reputationMode) {
        var result = set("reputation_mode", stmt -> stmt.bind(reputationMode.name()));
        if (result) {
            this.reputationMode = reputationMode;
        }
        return reputationMode;
    }

    /**
     * Sets the stack roles status for the guild.
     *
     * @param stackRoles whether stack roles is enabled
     * @return true if the stack roles status was successfully set, false otherwise
     */
    public boolean stackRoles(boolean stackRoles) {
        var result = set("stack_roles", stmt -> stmt.bind(stackRoles));
        if (result) {
            this.stackRoles.set(stackRoles);
        }
        return result;
    }

    /**
     * Sets the reset date for the guild.
     *
     * @param resetDate the reset date
     * @return true if the reset date was successfully set, false otherwise
     */
    public boolean resetDate(LocalDate resetDate) {
        var result = set("reset_date", stmt -> stmt.bind(resetDate == null ? null : Date.valueOf(resetDate)));
        if (result) {
            this.resetDate = resetDate;
        }
        return result;
    }

    /**
     * Retrieves the language for the guild.
     *
     * @return an Optional containing the DiscordLocale for the language, or an empty Optional if not set
     */
    public Optional<DiscordLocale> language() {
        return Optional.ofNullable(language);
    }

    /**
     * Checks if emoji debug is enabled for the guild.
     *
     * @return true if emoji debug is enabled, false otherwise
     */
    public boolean isEmojiDebug() {
        return emojiDebug;
    }

    /**
     * Checks if stack roles is enabled for the guild.
     *
     * @return true if stack roles is enabled, false otherwise
     */
    public boolean isStackRoles() {
        return stackRoles.get();
    }

    /**
     * Retrieves the AtomicBoolean representing the stack roles status for the guild.
     *
     * @return the AtomicBoolean representing the stack roles status
     */
    public AtomicBoolean stackRoles() {
        return stackRoles;
    }

    /**
     * Retrieves the reset date for the guild.
     *
     * @return the reset date
     */
    public LocalDate resetDate() {
        return resetDate;
    }

    /**
     * Retrieves the guild associated with this instance.
     *
     * @return the guild
     */
    @Override
    public Guild guild() {
        return settings.guild();
    }

    /**
     * Retrieves the ID of the guild associated with this instance.
     *
     * @return the guild ID
     */
    @Override
    public long guildId() {
        return settings.guildId();
    }

    /**
     * Sets a parameter in the database for the guild.
     *
     * @param parameter the parameter to set
     * @param builder the function to build the Call
     * @return true if the parameter was successfully set, false otherwise
     */
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

    /**
     * Retrieves the reputation mode for the guild.
     *
     * @return the ReputationMode
     */
    public ReputationMode reputationMode() {
        return reputationMode;
    }

    /**
     * Returns a pretty string representation of the general settings.
     *
     * @return a pretty string representation of the general settings
     */
    public String prettyString() {
        return """
                Stack roles: %s
                Emoji Debug: %s
                Language: %s
                Reputation Mode: %s
                """.stripIndent()
                   .formatted(stackRoles.get(), emojiDebug, language != null ? language.getLanguageName() : guild().getLocale().getLanguageName(), reputationMode.name());
    }
}
