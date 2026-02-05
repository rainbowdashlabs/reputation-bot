/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub;

public class ReputationPOJO {
    protected boolean reactionActive;
    protected boolean answerActive;
    protected boolean mentionActive;
    protected boolean fuzzyActive;
    protected boolean embedActive;
    protected boolean directActive;
    protected boolean commandActive;

    public ReputationPOJO(
            boolean reactionActive,
            boolean answerActive,
            boolean mentionActive,
            boolean fuzzyActive,
            boolean embedActive,
            boolean directActive,
            boolean commandActive) {
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
