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
import org.jetbrains.annotations.PropertyKey;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

/**
 * Manages the message settings for a guild.
 */
public class Messages implements GuildHolder {
    private final Settings settings;
    private boolean reactionConfirmation;

    /**
     * Constructs a Messages object with the specified settings and default reaction confirmation.
     *
     * @param settings the settings object
     */
    public Messages(Settings settings) {
        this(settings, true);
    }

    /**
     * Constructs a Messages object with the specified settings and reaction confirmation.
     *
     * @param settings the settings object
     * @param reactionConfirmation the reaction confirmation setting
     */
    public Messages(Settings settings, boolean reactionConfirmation) {
        this.settings = settings;
        this.reactionConfirmation = reactionConfirmation;
    }

    /**
     * Builds a Messages object from the specified settings and database row.
     *
     * @param settings the settings object
     * @param rs the database row
     * @return the constructed Messages object
     * @throws SQLException if a database access error occurs
     */
    public static Messages build(Settings settings, Row rs) throws SQLException {
        return new Messages(settings,
                rs.getBoolean("reaction_confirmation"));
    }

    /**
     * Sets the reaction confirmation setting.
     *
     * @param reactionConfirmation the new reaction confirmation setting
     * @return the updated reaction confirmation setting
     */
    public boolean reactionConfirmation(boolean reactionConfirmation) {
        var result = set("reaction_confirmation", stmt -> stmt.bind(reactionConfirmation));
        if (result) {
            this.reactionConfirmation = reactionConfirmation;
        }
        return this.reactionConfirmation;
    }

    /**
     * Returns the current reaction confirmation setting.
     *
     * @return true if reaction confirmation is enabled, false otherwise
     */
    public boolean isReactionConfirmation() {
        return reactionConfirmation;
    }

    /**
     * Returns the guild associated with this Messages object.
     *
     * @return the guild
     */
    @Override
    public Guild guild() {
        return settings.guild();
    }

    /**
     * Returns the guild ID associated with this Messages object.
     *
     * @return the guild ID
     */
    @Override
    public long guildId() {
        return settings.guildId();
    }

    /**
     * Sets a parameter in the message states table.
     *
     * @param parameter the parameter name
     * @param builder the function to build the query call
     * @return true if the parameter was successfully set, false otherwise
     */
    private boolean set(String parameter, Function<Call, Call> builder) {
        return query("""
                INSERT INTO message_states(guild_id, %s) VALUES (?, ?)
                ON CONFLICT(guild_id)
                    DO UPDATE SET %s = excluded.%s;
                """, parameter, parameter, parameter)
                .single(builder.apply(call().bind(guildId())))
                .insert()
                .changed();
    }

    /**
     * Returns a localized string representation of the message settings.
     *
     * @return the localized string representation
     */
    public String toLocalizedString() {
        var setting = List.of(
                getSetting("command.messages.states.message.option.reactionconfirmation.name", isReactionConfirmation())
        );

        return String.join("\n", setting);
    }

    /**
     * Returns a localized setting string.
     *
     * @param locale the locale key
     * @param object the setting value
     * @return the localized setting string
     */
    private String getSetting(@PropertyKey(resourceBundle = "locale") String locale, boolean object) {
        return String.format("$%s$: $%s$", locale, object ? "words.enabled" : "words.disabled");
    }

    /**
     * Returns a pretty string representation of the message settings.
     *
     * @return the pretty string representation
     */
    public String prettyString() {
        return """
                Reaction confirmation: %s
                """.stripIndent()
                   .formatted(reactionConfirmation);
    }
}
