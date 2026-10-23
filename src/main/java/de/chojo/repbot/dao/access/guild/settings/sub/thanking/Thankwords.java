/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import com.fasterxml.jackson.annotation.JsonSerializeAs;
import com.google.re2j.Pattern;
import com.google.re2j.PatternSyntaxException;
import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ThankwordCheckPOJO;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ThankwordsPOJO;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static org.slf4j.LoggerFactory.getLogger;

@JsonSerializeAs(ThankwordsPOJO.class)
public class Thankwords extends ThankwordsPOJO implements GuildHolder {
    private static final Logger log = getLogger(Thankwords.class);

    /**
     * Separates a thank word from its neighbours: any character that is not a letter, digit or underscore, or a
     * character of a script that is written without spaces between words. RE2 only knows ASCII word boundaries, so
     * {@code \b} would not find thank words in other scripts.
     */
    @Language("RegExp")
    private static final String BOUNDARY = "[^\\p{L}\\p{N}_]|[\\p{Han}\\p{Hiragana}\\p{Katakana}]";

    @Language("RegExp")
    private static final String THANKWORD = "(?:%s)";

    @Language("RegExp")
    private static final String PATTERN = "(?i)(?:^|%1$s)(?P<match>%2$s)(?:$|%1$s)";

    private final Thanking thanking;

    private final StampedLock lock;
    private volatile Pattern cachedPattern;

    public Thankwords(Thanking thanking, Set<String> thankwords) {
        super(thankwords);
        this.thanking = thanking;
        this.lock = new StampedLock();
        // as 'this' does not escape in this constructor,
        // we don't need a write-lock here
        this.cachedPattern = compilePattern();
    }

    @Override
    public GuildHolder guildHolder() {
        return thanking;
    }

    @Override
    public Set<String> words() {
        long stamp = lock.readLock();
        try {
            return Set.copyOf(thankwords);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public Pattern thankwordPattern() {
        // even if another thread has a write-lock, we either read the old pattern before the other thread compiles the
        // new one,
        // or we read the new one - both fine for our use
        return cachedPattern;
    }

    /**
     * Looks for a thank word in a text the same way the message analyzer does: the first match of the thank word
     * pattern in the lower-cased text.
     *
     * @param pattern the thank word pattern of a guild
     * @param text    the text to search
     * @return the part of the text that was recognised as a thank word
     */
    public static Optional<String> findThankword(Pattern pattern, String text) {
        if (pattern.pattern().isBlank()) return Optional.empty();
        var matcher = pattern.matcher(text.toLowerCase());
        if (!matcher.find()) return Optional.empty();
        return Optional.ofNullable(matcher.group("match"));
    }

    /**
     * Checks a text for thank words of this guild.
     *
     * @param text the text to check
     * @return the part of the text that was recognised as a thank word and the thank words that match it
     */
    public ThankwordCheckPOJO check(String text) {
        var match = findThankword(thankwordPattern(), text);
        if (match.isEmpty()) return ThankwordCheckPOJO.noMatch();
        var matchingWords = words().stream()
                .filter(word -> matchesWord(word, match.get()))
                .sorted()
                .toList();
        return new ThankwordCheckPOJO(true, match.get(), matchingWords);
    }

    public boolean add(String pattern) {
        long stamp = lock.writeLock();
        try {
            try {
                // Check whether the pattern can be compiled before adding it.
                compileWord(pattern);
            } catch (PatternSyntaxException e) {
                log.warn("Could not compile new pattern {} for guild {}", pattern, guildId(), e);
                return false;
            }
            var result = query("""
                    INSERT INTO
                        thankwords(guild_id, thankword) VALUES(?,?)
                            ON CONFLICT(guild_id, thankword)
                                DO NOTHING;
                    """)
                    .single(call().bind(guildId()).bind(pattern))
                    .update()
                    .changed();
            if (result) {
                thankwords.add(pattern);
                cachedPattern = compilePattern();
            }
            return result;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public boolean remove(String pattern) {
        long stamp = lock.writeLock();
        try {
            var result = query("""
                    DELETE FROM
                        thankwords
                    WHERE
                        guild_id = ?
                        AND thankword = ?
                    """)
                    .single(call().bind(guildId()).bind(pattern))
                    .update()
                    .changed();
            if (result) {
                thankwords.remove(pattern);
                cachedPattern = compilePattern();
            }
            return result;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public void apply(ThankwordsPOJO state) {
        for (String word : state.words()) {
            if (!thankwords.contains(word)) {
                add(word);
            }
        }

        for (String word : Set.copyOf(thankwords)) {
            if (!state.words().contains(word)) {
                remove(word);
            }
        }
    }

    public String prettyString() {
        return words().stream().map("`%s`"::formatted).collect(Collectors.joining(", "));
    }

    /**
     * Must be called in a write-lock if 'this' is accessible from other objects
     */
    private Pattern compilePattern() {
        if (thankwords.isEmpty()) return Pattern.compile("");
        return compilePattern(thankwords);
    }

    private Pattern compilePattern(Set<String> thankwords) {
        // Words predating the switch to RE2 might use syntax RE2 rejects. They are skipped to keep the guild working.
        var twPattern = thankwords.stream()
                .filter(this::compilesAsWord)
                .map(t -> String.format(THANKWORD, t))
                .collect(Collectors.joining("|"));
        if (twPattern.isEmpty()) return Pattern.compile("");
        return Pattern.compile(
                String.format(PATTERN, BOUNDARY, twPattern),
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    }

    private boolean compilesAsWord(String word) {
        try {
            compileWord(word);
            return true;
        } catch (PatternSyntaxException e) {
            log.warn("Skipping thankword {} of guild {} which is not a valid RE2 pattern", word, guildId(), e);
            return false;
        }
    }

    private boolean matchesWord(String word, String text) {
        try {
            return compileWord(word).matcher(text).find();
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    private Pattern compileWord(String word) {
        return Pattern.compile(
                String.format(PATTERN, BOUNDARY, String.format(THANKWORD, word)),
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    }
}
