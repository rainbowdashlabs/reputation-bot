/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReputationPOJO {
    protected boolean reactionActive;
    protected boolean answerActive;
    protected boolean mentionActive;
    protected boolean fuzzyActive;
    protected boolean embedActive;
    protected boolean directActive;
    protected boolean commandActive;

    /**
     * All values are required. A partial body is rejected instead of silently resetting the omitted settings, because
     * the settings are applied by difference.
     */
    @JsonCreator
    public ReputationPOJO(
            @JsonProperty(value = "reactionActive", required = true) boolean reactionActive,
            @JsonProperty(value = "answerActive", required = true) boolean answerActive,
            @JsonProperty(value = "mentionActive", required = true) boolean mentionActive,
            @JsonProperty(value = "fuzzyActive", required = true) boolean fuzzyActive,
            @JsonProperty(value = "embedActive", required = true) boolean embedActive,
            @JsonProperty(value = "directActive", required = true) boolean directActive,
            @JsonProperty(value = "commandActive", required = true) boolean commandActive) {
        this.reactionActive = reactionActive;
        this.answerActive = answerActive;
        this.mentionActive = mentionActive;
        this.fuzzyActive = fuzzyActive;
        this.embedActive = embedActive;
        this.directActive = directActive;
        this.commandActive = commandActive;
    }

    public boolean isReactionActive() {
        return reactionActive;
    }

    public boolean isAnswerActive() {
        return answerActive;
    }

    public boolean isMentionActive() {
        return mentionActive;
    }

    public boolean isFuzzyActive() {
        return fuzzyActive;
    }

    public boolean isEmbedActive() {
        return embedActive;
    }

    public boolean isDirectActive() {
        return directActive;
    }

    public boolean isCommandActive() {
        return commandActive;
    }
}
