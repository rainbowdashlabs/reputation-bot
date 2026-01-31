package de.chojo.repbot.web.sessions;

import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.web.pojo.GuildSessionPOJO;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

public class GuildSession {
    private final ShardManager shardManager;
    private final GuildRepository guildRepository;
    private final long guildId;
    private final long userId;

    public GuildSession(ShardManager shardManager, GuildRepository guildRepository, long guildId, long userId) {
        this.shardManager = shardManager;
        this.guildRepository = guildRepository;
        this.guildId = guildId;
        this.userId = userId;
    }

    private Guild guild() {
        return shardManager.getGuildById(guildId);
    }

    private RepGuild repGuild() {
        return guildRepository.guild(guild());
    }

    @NotNull
    public GuildSessionPOJO sessionData() {
        return GuildSessionPOJO.generate(guild(), guildRepository);
    }
}
