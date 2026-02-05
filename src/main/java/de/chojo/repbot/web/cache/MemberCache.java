package de.chojo.repbot.web.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.repbot.web.pojo.guild.MemberPOJO;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MemberCache {
    private final Cache<Key, MemberPOJO> cache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

    @NotNull
    public MemberPOJO get(Guild guild, String userId) {
        try {
            return cache.get(new Key(guild.getId(), userId), () -> generate(guild, userId));
        } catch (ExecutionException e) {
            return MemberPOJO.generate(userId);
        }
    }

    private MemberPOJO generate(Guild guild, String memberId) {
        try {
            Member member = guild.retrieveMemberById(memberId).complete();
            return MemberPOJO.generate(member);
        } catch (ErrorResponseException e) {
            return MemberPOJO.generate(memberId);
        }
    }

    record Key(String guildId, String userId) {
    }
}
