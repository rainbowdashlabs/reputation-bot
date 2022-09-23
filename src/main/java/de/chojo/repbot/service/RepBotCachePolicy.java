package de.chojo.repbot.service;

import de.chojo.repbot.commands.roles.Roles;
import de.chojo.repbot.commands.scan.Scan;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class RepBotCachePolicy implements MemberCachePolicy, Runnable {
    public static final int CACHE_DURATION = 30;
    private final HashMap<Long, Instant> seen = new HashMap<>();
    private final Scan scan;

    private final Roles roles;

    public RepBotCachePolicy(Scan scan, Roles roles) {
        this.scan = scan;
        this.roles = roles;
    }

    public void seen(Member member) {
        seen.put(member.getIdLong(), Instant.now());
    }

    @Override
    public boolean cacheMember(@NotNull Member member) {
        if (MemberCachePolicy.VOICE.cacheMember(member)) {
            return true;
        }

        if (MemberCachePolicy.OWNER.cacheMember(member)) {
            return true;
        }

        // Hold members during running scan
        if (scan.isRunning(member.getGuild())) {
            return true;
        }

        // Hold members during role refresh
        if (roles.refreshActive(member.getGuild())) {
            return true;
        }

        // We always want to keep members we see for the first time
        if (!seen.containsKey(member.getIdLong())) {
            seen.put(member.getIdLong(), Instant.now());
            return true;
        }

        // Check if we have seen this member recently
        if (seen.get(member.getIdLong()).isAfter(oldest())) {
            return true;
        }

        seen.remove(member.getIdLong());
        return false;
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
