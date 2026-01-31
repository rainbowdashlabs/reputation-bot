package de.chojo.repbot.web.sessions;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.Hashing;
import de.chojo.repbot.dao.provider.GuildRepository;
import io.javalin.http.Context;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.chojo.repbot.util.States.TEST_MODE;

public class SessionService {
    private final Cache<String, GuildSession> guildSessions = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
    private final GuildRepository guildRepository;

    public SessionService(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    public Optional<GuildSession> getGuildSession(Context ctx) {
        String authorization = ctx.header("Authorization");
        if (authorization == null) return Optional.empty();
        return Optional.ofNullable(guildSessions.getIfPresent(authorization));
    }

    public String createGuildSession(Guild guild, Member member) {
        var randomString = ThreadLocalRandom.current().ints(10, 'a', 'z')
                                            .limit(25)
                                            .mapToObj(Character::toString)
                                            .collect(Collectors.joining());
        String key;
        if (TEST_MODE) {
            key = guild.getId();
        } else {
            key = Hashing.sha256().hashBytes("%s%s%s".formatted(guild.getId(), member.getId(), randomString).getBytes(StandardCharsets.UTF_8)).toString();
        }
        guildSessions.put(key, new GuildSession(guild.getJDA().getShardManager(), guildRepository, guild.getIdLong(), member.getIdLong()));
        return key;
    }
}
