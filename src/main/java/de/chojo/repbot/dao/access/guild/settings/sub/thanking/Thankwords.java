/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.components.GuildHolder;
import net.dv8tion.jda.api.entities.Guild;
import org.intellij.lang.annotations.Language;

import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

/**
 * Class representing the thank words settings for a guild.
 */
public class Thankwords implements GuildHolder {
    /**
     * Regular expression pattern for a thank word.
     */
    @Language("RegExp")
    private static final String THANKWORD = "(?:^|\\b)%s(?:$|\\b)";

    /**
     * Regular expression pattern for matching thank words.
     */
    @Language("RegExp")
    private static final String PATTERN = "(?i)(?<match>%s)";

    private final Thanking thanking;
    private final Set<String> thankwords;
    private final StampedLock lock;
    private volatile Pattern cachedPattern;

    /**
     * Constructs a new Thankwords instance.
     *
     * @param thanking the thanking settings
     * @param thankwords the set of thank words
     */
    public Thankwords(Thanking thanking, Set<String> thankwords) {
        this.thanking = thanking;
        this.thankwords = thankwords;
        this.lock = new StampedLock();
        // as 'this' does not escape in this constructor,
        // we don't need a write-lock here
        this.cachedPattern = compilePattern();
    }

    /**
     * Retrieves the guild associated with the thank words settings.
     *
     * @return the guild associated with the thank words settings
     */
    @Override
    public Guild guild() {
        return thanking.guild();
    }

    /**
     * Retrieves the guild ID associated with the thank words settings.
     *
     * @return the guild ID associated with the thank words settings
     */
    @Override
    public long guildId() {
        return thanking.guildId();
    }

    /**
     * Retrieves the set of thank words.
     *
     * @return the set of thank words
     */
    public Set<String> words() {
        long stamp = lock.readLock();
        try {
            return Set.copyOf(thankwords);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    /**
     * Retrieves the compiled pattern for matching thank words.
     *
     * @return the compiled pattern for matching thank words
     */
    public Pattern thankwordPattern() {
        // even if another thread has a write-lock, we either read the old pattern before the other thread compiles the new one,
        // or we read the new one - both fine for our use
        return cachedPattern;
    }

    /**
     * Compiles the pattern for matching thank words.
     * Must be called in a write-lock if 'this' is accessible from other objects.
     *
     * @return the compiled pattern for matching thank words
     */
    private Pattern compilePattern() {
        if (thankwords.isEmpty()) return Pattern.compile("");
        var twPattern = thankwords.stream()
                .map(t -> String.format(THANKWORD, t))
                .collect(Collectors.joining("|"));
        return Pattern.compile(String.format(PATTERN, twPattern),
                Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL + Pattern.COMMENTS);
    }

    /**
     * Adds a new thank word to the set.
     *
     * @param pattern the thank word pattern to add
     * @return true if the thank word was added, false otherwise
     */
    public boolean add(String pattern) {
        long stamp = lock.writeLock();
        try {
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

    /**
     * Removes a thank word from the set.
     *
     * @param pattern the thank word pattern to remove
     * @return true if the thank word was removed, false otherwise
     */
    public boolean remove(String pattern) {
        long stamp = lock.writeLock();
        try {
            var result = query("""
                    DELETE FROM
                        thankwords
                    WHERE
                        guild_id = ?
                        AND thankword = ?
                    """).single(call().bind(guildId()).bind(pattern))
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

    /**
     * Retrieves a pretty string representation of the thank words.
     *
     * @return a pretty string representation of the thank words
     */
    public String prettyString() {
        return words().stream().map("`%s`"::formatted).collect(Collectors.joining(", "));
    }
}
