package de.chojo.repbot.service;

import de.chojo.repbot.commands.Scan;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

public class RepBotCachePolicy implements MemberCachePolicy, Runnable {
    public static final int CACHE_DURATION = 30;
    private final HashMap<Long, Instant> seen = new HashMap<>();
    private final Scan scan;

    public RepBotCachePolicy(Scan scan) {
        this.scan = scan;
    }

    public void seen(Member member) {
        seen.put(member.getIdLong(), Instant.now());
    }

    @Override
    public boolean cacheMember(@NotNull Member member) {
        if (MemberCachePolicy.ONLINE.cacheMember(member)) {
            return true;
        }

        if (MemberCachePolicy.VOICE.cacheMember(member)) {
            return true;
        }

        if (MemberCachePolicy.OWNER.cacheMember(member)) {
            return true;
        }

        if (scan.isRunning(member.getGuild())) {
            return true;
        }

        if (!seen.containsKey(member.getIdLong())) {
            seen.put(member.getIdLong(), Instant.now());
            return true;
        }

        if (seen.get(member.getIdLong()).isBefore(oldest())) {
            seen.remove(member.getIdLong());
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        clean();
    }

    public synchronized void clean() {
        Set<Long> remove = new HashSet<>();
        var oldest = oldest();
        seen.forEach((key, value) -> {
            if (value.isBefore(oldest)) {
                remove.add(key);
            }
        });
        remove.forEach(seen::remove);
    }

    private Instant oldest() {
        return Instant.now().minus(CACHE_DURATION, ChronoUnit.MINUTES);
    }
}
