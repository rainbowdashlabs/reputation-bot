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
 * Represents the reputation settings for a guild.
 */
public class Reputation implements GuildHolder {
    private final Settings settings;
    private boolean reactionActive;
    private boolean answerActive;
    private boolean mentionActive;
    private boolean fuzzyActive;
    private boolean embedActive;
    private boolean directActive;

    /**
     * Constructs a Reputation object with the specified settings and default values.
     *
     * @param settings the settings object
     */
    public Reputation(Settings settings) {
        this(settings, true, true, true, true, true, false);
    }

    /**
     * Constructs a Reputation object with the specified settings and values.
     *
     * @param settings the settings object
     * @param reactionActive whether reactions are active
     * @param answerActive whether answers are active
     * @param mentionActive whether mentions are active
     * @param fuzzyActive whether fuzzy matching is active
     * @param embedActive whether embeds are active
     * @param directActive whether direct messages are active
     */
    public Reputation(Settings settings, boolean reactionActive, boolean answerActive, boolean mentionActive, boolean fuzzyActive, boolean embedActive, boolean directActive) {
        this.settings = settings;
        this.reactionActive = reactionActive;
        this.answerActive = answerActive;
        this.mentionActive = mentionActive;
        this.fuzzyActive = fuzzyActive;
        this.embedActive = embedActive;
        this.directActive = directActive;
    }

    /**
     * Builds a Reputation object from the specified settings and row.
     *
     * @param settings the settings object
     * @param rs the row object
     * @return the Reputation object
     * @throws SQLException if a database access error occurs
     */
    public static Reputation build(Settings settings, Row rs) throws SQLException {
        return new Reputation(settings,
                rs.getBoolean("reactions_active"),
                rs.getBoolean("answer_active"),
                rs.getBoolean("mention_active"),
                rs.getBoolean("fuzzy_active"),
                rs.getBoolean("embed_active"),
                rs.getBoolean("skip_single_embed"));
    }

    /**
     * Returns whether reactions are active.
     *
     * @return true if reactions are active, false otherwise
     */
    public boolean isReactionActive() {
        return reactionActive;
    }

    /**
     * Returns whether answers are active.
     *
     * @return true if answers are active, false otherwise
     */
    public boolean isAnswerActive() {
        return answerActive;
    }

    /**
     * Returns whether mentions are active.
     *
     * @return true if mentions are active, false otherwise
     */
    public boolean isMentionActive() {
        return mentionActive;
    }

    /**
     * Returns whether fuzzy matching is active.
     *
     * @return true if fuzzy matching is active, false otherwise
     */
    public boolean isFuzzyActive() {
        return fuzzyActive;
    }

    /**
     * Returns whether embeds are active.
     *
     * @return true if embeds are active, false otherwise
     */
    public boolean isEmbedActive() {
        return embedActive;
    }

    /**
     * Returns whether direct messages are active.
     *
     * @return true if direct messages are active, false otherwise
     */
    public boolean isDirectActive() {
        return directActive;
    }

    /**
     * Sets whether embeds are active.
     *
     * @param embedActive true to activate embeds, false to deactivate
     * @return the updated embed active status
     */
    public boolean embedActive(boolean embedActive) {
        var result = set("embed_active", stmt -> stmt.bind(embedActive));
        if (result) {
            this.embedActive = embedActive;
        }
        return this.embedActive;
    }

    /**
     * Sets whether reactions are active.
     *
     * @param reactionActive true to activate reactions, false to deactivate
     * @return the updated reaction active status
     */
    public boolean reactionActive(boolean reactionActive) {
        var result = set("reactions_active", stmt -> stmt.bind(reactionActive));
        if (result) {
            this.reactionActive = reactionActive;
        }
        return this.reactionActive;
    }

    /**
     * Sets whether answers are active.
     *
     * @param answerActive true to activate answers, false to deactivate
     * @return the updated answer active status
     */
    public boolean answerActive(boolean answerActive) {
        var result = set("answer_active", stmt -> stmt.bind(answerActive));
        if (result) {
            this.answerActive = answerActive;
        }
        return this.answerActive;
    }

    /**
     * Sets whether mentions are active.
     *
     * @param mentionActive true to activate mentions, false to deactivate
     * @return the updated mention active status
     */
    public boolean mentionActive(boolean mentionActive) {
        var result = set("mention_active", stmt -> stmt.bind(mentionActive));
        if (result) {
            this.mentionActive = mentionActive;
        }
        return this.mentionActive;
    }

    /**
     * Sets whether fuzzy matching is active.
     *
     * @param fuzzyActive true to activate fuzzy matching, false to deactivate
     * @return the updated fuzzy active status
     */
    public boolean fuzzyActive(boolean fuzzyActive) {
        var result = set("fuzzy_active", stmt -> stmt.bind(fuzzyActive));
        if (result) {
            this.fuzzyActive = fuzzyActive;
        }
        return this.fuzzyActive;
    }

    /**
     * Sets whether direct messages are active.
     *
     * @param directActive true to activate direct messages, false to deactivate
     * @return the updated direct active status
     */
    public boolean directActive(boolean directActive) {
        var result = set("skip_single_embed", stmt -> stmt.bind(directActive));
        if (result) {
            this.directActive = directActive;
        }
        return this.directActive;
    }

    /**
     * Returns a localized string representation of the reputation settings.
     *
     * @return the localized string representation
     */
    public String toLocalizedString() {
        var setting = List.of(
                getSetting("command.repsettings.info.message.option.byreaction.name", isReactionActive()),
                getSetting("command.repsettings.info.message.option.byanswer.name", isAnswerActive()),
                getSetting("command.repsettings.info.message.option.bymention.name", isMentionActive()),
                getSetting("command.repsettings.info.message.option.byfuzzy.name", isFuzzyActive()),
                getSetting("command.repsettings.info.message.option.byembed.name", isEmbedActive()),
                getSetting("command.repsettings.info.message.option.emojidebug.name", settings.general()
                                                                                              .isEmojiDebug()),
                getSetting("command.repsettings.info.message.option.skipsingletarget.name", settings.reputation()
                                                                                                    .isDirectActive()),
                getSetting("command.repsettings.info.message.option.reputationmode.name", settings.general()
                                                                                                  .reputationMode()
                                                                                                  .localeCode())
        );

        return String.join("\n", setting);
    }

    /**
     * Returns a localized setting string.
     *
     * @param locale the locale key
     * @param object the object to be localized
     * @return the localized setting string
     */
    private String getSetting(@PropertyKey(resourceBundle = "locale") String locale, boolean object) {
        return getSetting(locale, object ? "words.enabled" : "words.disabled");
    }

    /**
     * Returns a localized setting string.
     *
     * @param locale the locale key
     * @param object the object to be localized
     * @return the localized setting string
     */
    private String getSetting(@PropertyKey(resourceBundle = "locale") String locale, String object) {
        return String.format("$%s$: $%s$", locale, object);
    }

    /**
     * Returns the guild associated with the settings.
     *
     * @return the guild
     */
    @Override
    public Guild guild() {
        return settings.guild();
    }

    /**
     * Returns the guild ID associated with the settings.
     *
     * @return the guild ID
     */
    @Override
    public long guildId() {
        return settings.guildId();
    }

    /**
     * Sets a parameter in the reputation settings.
     *
     * @param parameter the parameter to set
     * @param builder the function to build the call
     * @return true if the parameter was set successfully, false otherwise
     */
    private boolean set(String parameter, Function<Call, Call> builder) {
        return query("""
                INSERT INTO reputation_settings(guild_id, %s) VALUES (?, ?)
                ON CONFLICT(guild_id)
                    DO UPDATE SET %s = excluded.%s;
                """, parameter, parameter, parameter)
                .single(builder.apply(call().bind(guildId())))
                .insert()
                .changed();
    }

    /**
     * Returns a pretty string representation of the reputation settings.
     *
     * @return the pretty string representation
     */
    public String prettyString() {
        return """
                Reaction active: %s
                Answer active: %s
                Mention active: %s
                Fuzzy active: %s
                Embed active: %s
                Skip single embed: %s
                """.formatted(reactionActive, answerActive, mentionActive, fuzzyActive, embedActive, directActive)
                   .stripIndent();
    }
}
