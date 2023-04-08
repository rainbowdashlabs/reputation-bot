package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Guild;
import org.intellij.lang.annotations.Language;

import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Thankwords extends QueryFactory implements GuildHolder {
    @Language("RegExp")
    private static final String THANKWORD = "(?:^|\\b)%s(?:$|\\b)";
    @Language("RegExp")
    private static final String PATTERN = "(?i)(?<match>%s)";

    private final Thanking thanking;

    private final Set<String> thankwords;
    private final StampedLock lock;
    private volatile Pattern cachedPattern;

    public Thankwords(Thanking thanking, Set<String> thankwords) {
        super(thanking);
        this.thanking = thanking;
        this.thankwords = thankwords;
        this.lock = new StampedLock();
    }

    @Override
    public Guild guild() {
        return thanking.guild();
    }

    @Override
    public long guildId() {
        return thanking.guildId();
    }

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

    /**
     * Must be called in a write-lock
     */
    private Pattern compilePattern() {
        if (thankwords.isEmpty()) return Pattern.compile("");
        var twPattern = thankwords.stream()
                .map(t -> String.format(THANKWORD, t))
                .collect(Collectors.joining("|"));
        return Pattern.compile(String.format(PATTERN, twPattern),
                Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL + Pattern.COMMENTS);
    }

    public boolean add(String pattern) {
        long stamp = lock.writeLock();
        try {
            var result = builder().query("""
                    INSERT INTO
                        thankwords(guild_id, thankword) VALUES(?,?)
                            ON CONFLICT(guild_id, thankword)
                                DO NOTHING;
                    """)
                    .parameter(stmt -> stmt.setLong(guildId()).setString(pattern))
                    .update()
                    .sendSync()
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
            var result = builder().query("""
                    DELETE FROM
                        thankwords
                    WHERE
                        guild_id = ?
                        AND thankword = ?
                    """).parameter(stmt -> stmt.setLong(guildId()).setString(pattern))
                    .update()
                    .sendSync()
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

    public String prettyString() {
        return words().stream().map("`%s`"::formatted).collect(Collectors.joining(", "));
    }
}
