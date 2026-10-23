/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.analyzer;

import com.google.re2j.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class MessageAnalyzerTest {
    private static final Pattern THANKS = Pattern.compile("(?i)thanks");

    private static List<String> words(String message) {
        return List.of(message.split(" "));
    }

    @Test
    void findsThankWordsAtTheirPosition() {
        var words = words("a b c d e f g h i j k l thanks m");
        Assertions.assertEquals(List.of(12), MessageAnalyzer.thankWordIndices(words, THANKS));
    }

    @Test
    void findsEveryThankWord() {
        var words = words("thanks a thanks b");
        Assertions.assertEquals(List.of(0, 2), MessageAnalyzer.thankWordIndices(words, THANKS));
    }

    @Test
    void surroundingWordsLookAroundTheThankWord() {
        var words = words("a b c d e f g h i j k l thanks m n o p q r s t");
        Assertions.assertEquals(
                List.of("g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r"),
                MessageAnalyzer.surroundingWords(words, 12));
    }

    @Test
    void surroundingWordsAtTheEdges() {
        Assertions.assertEquals(List.of("bob"), MessageAnalyzer.surroundingWords(words("thanks bob"), 0));
        Assertions.assertEquals(List.of("bob"), MessageAnalyzer.surroundingWords(words("bob thanks"), 1));
        Assertions.assertEquals(List.of(), MessageAnalyzer.surroundingWords(words("thanks"), 0));
    }
}
