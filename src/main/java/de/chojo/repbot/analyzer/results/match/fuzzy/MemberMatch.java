/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.analyzer.results.match.fuzzy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a match result for a member with a word, name, nickname, and score.
 */
public class MemberMatch {
    private final String word;
    private final String name;
    private final String nickname;
    private final double score;

    /**
     * Constructs a new MemberMatch with the specified word, name, nickname, and score.
     *
     * @param word the matched word
     * @param name the member's name
     * @param nickname the member's nickname
     * @param score the match score
     */
    @JsonCreator
    public MemberMatch(@JsonProperty("word") String word, @JsonProperty("name") String name, @JsonProperty("nickname") String nickname, @JsonProperty("score") double score) {
        this.word = word;
        this.name = name;
        this.nickname = nickname;
        this.score = score;
    }

    /**
     * Returns the matched word.
     *
     * @return the matched word
     */
    public String word() {
        return word;
    }

    /**
     * Returns the member's name.
     *
     * @return the member's name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the member's nickname.
     *
     * @return the member's nickname
     */
    public String nickname() {
        return nickname;
    }

    /**
     * Returns the match score.
     *
     * @return the match score
     */
    public double score() {
        return score;
    }

    /**
     * Returns a string representation of the match result.
     *
     * @return a string representation of the match result
     */
    public String asString() {
        return "`%s` ➜ %s (%s) | Score %.03f".formatted(word, name, nickname, score);
    }
}
