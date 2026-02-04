/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import com.fasterxml.jackson.annotation.JsonSerializeAs;
import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ThankwordsPOJO;
import net.dv8tion.jda.api.entities.Guild;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static org.slf4j.LoggerFactory.getLogger;

@JsonSerializeAs(ThankwordsPOJO.class)
public class Thankwords extends ThankwordsPOJO implements GuildHolder {
    private static final Logger log = getLogger(Thankwords.class);
    @Language("RegExp")
    private static final String THANKWORD = "(?:^|\\b)%s(?:$|\\b)";
    @Language("RegExp")
    private static final String PATTERN = "(?i)(?<match>%s)";

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
    public Guild guild() {
        return thanking.guild();
    }

    @Override
    public long guildId() {
        return thanking.guildId();
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
        // even if another thread has a write-lock, we either read the old pattern before the other thread compiles the new one,
        // or we read the new one - both fine for our use
        return cachedPattern;
    }

    public boolean add(String pattern) {
        long stamp = lock.writeLock();
        try {
            try {
                // Check whether the pattern can be compiled after adding it.
                HashSet<String> thankwords = new HashSet<>(this.thankwords);
                thankwords.add(pattern);
                compilePattern(thankwords);
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
        var twPattern = thankwords.stream()
                                  .map(t -> String.format(THANKWORD, t))
                                  .collect(Collectors.joining("|"));
        return Pattern.compile(String.format(PATTERN, twPattern),
                Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL);

    }
}
