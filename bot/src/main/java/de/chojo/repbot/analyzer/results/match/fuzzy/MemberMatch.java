/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.analyzer.results.match.fuzzy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MemberMatch {
    private final String word;
    private final String name;
    private final String nickname;
    private final double score;

    @JsonCreator
    public MemberMatch(
            @JsonProperty("word") String word,
            @JsonProperty("name") String name,
            @JsonProperty("nickname") String nickname,
            @JsonProperty("score") double score) {
        this.word = word;
        this.name = name;
        this.nickname = nickname;
        this.score = score;
    }

    public String word() {
        return word;
    }

    public String name() {
        return name;
    }

    public String nickname() {
        return nickname;
    }

    public double score() {
        return score;
    }

    public String asString() {
        return "`%s` ➜ %s (%s) | Score %.03f".formatted(word, name, nickname, score);
    }
}
