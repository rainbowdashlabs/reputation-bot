/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub.integrationbypass;

import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.sadu.mapper.wrapper.Row;

import java.sql.SQLException;

public class Bypass {
    long integrationId;
    boolean allowReactions;
    boolean allowAnswer;
    boolean allowMention;
    boolean allowFuzzy;
    boolean allowDirect;
    boolean ignoreCooldown;
    boolean ignoreLimit;
    boolean ignoreContext;

    public Bypass() {}

    public Bypass(Row row) throws SQLException {
        integrationId = row.getLong("integration_id");
        allowReactions = row.getBoolean("allow_reactions");
        allowAnswer = row.getBoolean("allow_answer");
        allowMention = row.getBoolean("allow_mention");
        allowFuzzy = row.getBoolean("allow_fuzzy");
        allowDirect = row.getBoolean("allow_direct");
        ignoreCooldown = row.getBoolean("ignore_cooldown");
        ignoreLimit = row.getBoolean("ignore_limit");
        ignoreContext = row.getBoolean("ignore_context");
    }

    /**
     * Get the integration id
     *
     * @return long
     */
    public long integrationId() {
        return integrationId;
    }

    /**
     * Allow giving a reputation via reactions
     *
     * @return true if reactions are allowed, false otherwise
     */
    public boolean allowReactions() {
        return allowReactions;
    }

    /**
     * Allow giving a reputation via answering a message
     *
     * @return boolean
     */
    public boolean allowAnswer() {
        return allowAnswer;
    }

    /**
     * Allow giving a reputation via mentioning a user
     *
     * @return boolean
     */
    public boolean allowMention() {
        return allowMention;
    }

    /**
     * Allow giving a reputation via fuzzy matching
     *
     * @return boolean
     */
    public boolean allowFuzzy() {
        return allowFuzzy;
    }

    /**
     * Ignore cooldown between users
     *
     * @return boolean
     */
    public boolean ignoreCooldown() {
        return ignoreCooldown;
    }

    /**
     * Ignore any hourly limit set
     *
     * @return boolean
     */
    public boolean ignoreLimit() {
        return ignoreLimit;
    }

    /**
     * Allow giving Reputation via a direct embed. No embed will ever be sent to a bot.
     *
     * @return boolean
     */
    public boolean allowDirect() {
        return allowDirect;
    }

    /**
     * Allows bypassing context checks
     *
     * @return boolean
     */
    public boolean ignoreContext() {
        return ignoreContext;
    }

    public boolean isEnabled(ThankType bypassed) {
        return switch (bypassed) {
            case FUZZY -> allowFuzzy();
            case MENTION -> allowMention();
            case ANSWER -> allowAnswer();
            case DIRECT -> allowDirect();
            case REACTION -> allowReactions();
            case EMBED, COMMAND -> false;
        };
    }
}
