/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.components.GuildHolder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ThankwordPatternTest {

    private static Thankwords thankwords(String... words) {
        var thanking = new Thanking(null) {
            @Override
            public GuildHolder guildHolder() {
                return this;
            }

            @Override
            public long guildId() {
                return 0;
            }
        };
        return new Thankwords(thanking, new HashSet<>(Set.of(words)));
    }

    @Test
    void matchesThankword() {
        var pattern = thankwords("thanks", "ty").thankwordPattern();
        var matcher = pattern.matcher("thanks a lot!");
        Assertions.assertTrue(matcher.find());
        Assertions.assertEquals("thanks", matcher.group("match"));
    }

    @Test
    void matchesCaseInsensitive() {
        var pattern = thankwords("thanks").thankwordPattern();
        Assertions.assertTrue(pattern.matcher("THANKS mate").find());
    }

    @Test
    void requiresWordBoundary() {
        var pattern = thankwords("ty").thankwordPattern();
        Assertions.assertFalse(pattern.matcher("empty message").find());
        Assertions.assertTrue(pattern.matcher("ok ty!").find());
    }

    @Test
    void matchesThankwordsInOtherScripts() {
        var pattern = thankwords("спасиб[ао]?", "ευχαριστώ", "감사합니다").thankwordPattern();
        var matcher = pattern.matcher("спасибо друг");
        Assertions.assertTrue(matcher.find());
        Assertions.assertEquals("спасибо", matcher.group("match"));
        Assertions.assertTrue(pattern.matcher("σας ευχαριστώ πολύ").find());
        Assertions.assertTrue(pattern.matcher("정말 감사합니다!").find());
        Assertions.assertFalse(pattern.matcher("неспасибо").find());
    }

    @Test
    void matchesThankwordsInScriptsWithoutSpaces() {
        var pattern = thankwords("ありがと[うゆ]?(?:ございま[すした])?", "谢+").thankwordPattern();
        var matcher = pattern.matcher("本当にありがとうございます");
        Assertions.assertTrue(matcher.find());
        Assertions.assertEquals("ありがとうございます", matcher.group("match"));
        Assertions.assertTrue(pattern.matcher("非常谢谢你").find());
    }

    @Test
    void matchesOnlyTheWordAtLineEdges() {
        var pattern = thankwords("thanks").thankwordPattern();
        var matcher = pattern.matcher("hey,thanks\nbye");
        Assertions.assertTrue(matcher.find());
        Assertions.assertEquals("thanks", matcher.group("match"));
        Assertions.assertFalse(pattern.matcher("thanksgiving").find());
    }

    @Test
    void emptyWordsYieldBlankPattern() {
        Assertions.assertTrue(thankwords().thankwordPattern().pattern().isBlank());
    }

    @Test
    void skipsWordsInvalidInRe2() {
        // Lookahead is valid in java.util.regex but not in RE2. Legacy words like this must not break the guild.
        var pattern = thankwords("(?=thanks)", "ty").thankwordPattern();
        Assertions.assertTrue(pattern.matcher("ok ty!").find());
        var onlyInvalid = thankwords("(?=thanks)").thankwordPattern();
        Assertions.assertTrue(onlyInvalid.pattern().isBlank());
    }

    @Test
    void catastrophicBacktrackingPatternMatchesInLinearTime() {
        // (a+)+c against a long run of 'a's hangs java.util.regex for hours. RE2 must finish instantly.
        var pattern = thankwords("(a+)+c").thankwordPattern();
        var message = "a".repeat(10_000) + "b";
        Assertions.assertTimeoutPreemptively(
                Duration.ofSeconds(2),
                () -> Assertions.assertFalse(pattern.matcher(message).find()));
    }

    @Test
    void checkReportsMatchAndMatchingWords() {
        var result = thankwords("thanks?", "thx", "ty").check("Thanks for the help!");
        Assertions.assertTrue(result.matched());
        Assertions.assertEquals("thanks", result.match());
        Assertions.assertEquals(List.of("thanks?"), result.thankwords());
    }

    @Test
    void checkWithoutThankword() {
        var result = thankwords("thanks").check("hello there");
        Assertions.assertFalse(result.matched());
        Assertions.assertNull(result.match());
        Assertions.assertTrue(result.thankwords().isEmpty());
    }

    @Test
    void checkWithoutConfiguredThankwords() {
        Assertions.assertFalse(thankwords().check("thanks").matched());
    }
}
