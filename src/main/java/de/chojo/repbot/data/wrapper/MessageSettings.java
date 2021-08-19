package de.chojo.repbot.data.wrapper;

import java.util.Set;

public class MessageSettings {
    private boolean reactionActive;
    private boolean answerActive;
    private boolean mentionActive;
    private boolean fuzzyActive;
    private boolean embedActive;

    public MessageSettings() {
        reactionActive = true;
        answerActive = true;
        mentionActive = true;
        fuzzyActive = true;
        embedActive = true;
    }

    public MessageSettings(boolean reactionActive, boolean answerActive, boolean mentionActive, boolean fuzzyActive, boolean embedActive) {
        this.reactionActive = reactionActive;
        this.answerActive = answerActive;
        this.mentionActive = mentionActive;
        this.fuzzyActive = fuzzyActive;
        this.embedActive = embedActive;
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

    public void embedActive(boolean embedActive) {
        this.embedActive = embedActive;
    }

    public void reactionActive(boolean reactionActive) {
        this.reactionActive = reactionActive;
    }

    public void answerActive(boolean answerActive) {
        this.answerActive = answerActive;
    }

    public void mentionActive(boolean mentionActive) {
        this.mentionActive = mentionActive;
    }

    public void fuzzyActive(boolean fuzzyActive) {
        this.fuzzyActive = fuzzyActive;
    }
}
