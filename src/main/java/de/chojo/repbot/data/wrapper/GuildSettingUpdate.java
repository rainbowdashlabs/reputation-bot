package de.chojo.repbot.data.wrapper;

import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nullable;

public class GuildSettingUpdate {
    private final Guild guild;
    @Nullable
    private final Integer maxMessageAge;
    @Nullable
    private final String reaction;
    @Nullable
    private final Boolean reactionsActive;
    @Nullable
    private final Boolean answerActive;
    @Nullable
    private final Boolean mentionActive;
    @Nullable
    private final Boolean fuzzyActive;
    @Nullable
    private final Integer cooldown;

    private GuildSettingUpdate(Guild guild, @Nullable Integer maxMessageAge, @Nullable String reaction,
                               @Nullable Boolean reactionsActive, @Nullable Boolean answerActive, @Nullable Boolean mentionActive,
                               @Nullable Boolean fuzzyActive, @Nullable Integer cooldown) {

        this.guild = guild;
        this.maxMessageAge = maxMessageAge;
        this.reaction = reaction;
        this.reactionsActive = reactionsActive;
        this.answerActive = answerActive;
        this.mentionActive = mentionActive;
        this.fuzzyActive = fuzzyActive;
        this.cooldown = cooldown;
    }

    public static Builder builder(Guild guild) {
        return new Builder(guild);
    }

    public Guild guild() {
        return guild;
    }

    @Nullable
    public Integer maxMessageAge() {
        return maxMessageAge;
    }

    @Nullable
    public String reaction() {
        return reaction;
    }

    @Nullable
    public Boolean reactionsActive() {
        return reactionsActive;
    }

    @Nullable
    public Boolean answerActive() {
        return answerActive;
    }

    @Nullable
    public Boolean mentionActive() {
        return mentionActive;
    }

    @Nullable
    public Boolean fuzzyActive() {
        return fuzzyActive;
    }

    @Nullable
    public Integer cooldown() {
        return cooldown;
    }

    public static class Builder {
        private final Guild guild;
        @Nullable
        private Integer maxMessageAge;
        @Nullable
        private String reaction;
        @Nullable
        private Boolean reactionsActive;
        @Nullable
        private Boolean answerActive;
        @Nullable
        private Boolean mentionActive;
        @Nullable
        private Boolean fuzzyActive;
        @Nullable
        private Integer cooldown;

        public Builder(Guild guild) {
            this.guild = guild;
        }

        public Builder maxMessageAge(@Nullable Integer maxMessageAge) {
            this.maxMessageAge = maxMessageAge;
            return this;
        }

        public Builder reaction(@Nullable String reaction) {
            this.reaction = reaction;
            return this;
        }

        public Builder reactionsActive(@Nullable Boolean reactionsActive) {
            this.reactionsActive = reactionsActive;
            return this;
        }

        public Builder answerActive(@Nullable Boolean answerActive) {
            this.answerActive = answerActive;
            return this;
        }

        public Builder mentionActive(@Nullable Boolean mentionActive) {
            this.mentionActive = mentionActive;
            return this;
        }

        public Builder fuzzyActive(@Nullable Boolean fuzzyActive) {
            this.fuzzyActive = fuzzyActive;
            return this;
        }

        public Builder cooldown(@Nullable Integer cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        public GuildSettingUpdate build() {
            return new GuildSettingUpdate(guild, maxMessageAge, reaction, reactionsActive, answerActive, mentionActive, fuzzyActive, cooldown);
        }
    }
}
