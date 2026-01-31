package de.chojo.repbot.web.pojo.guild;

import net.dv8tion.jda.api.entities.Guild;

import java.util.List;

public class GuildPOJO {
    List<RolePOJO> roles;
    ChannelViewPOJO channels;
    List<ReactionPOJO> reactions;

    public GuildPOJO(List<RolePOJO> roles, ChannelViewPOJO channels, List<ReactionPOJO> reactions) {
        this.roles = roles;
        this.channels = channels;
        this.reactions = reactions;
    }

    public static GuildPOJO generate(Guild guild) {
        List<RolePOJO> roles = guild.getRoles().stream().filter(r -> !r.isPublicRole()).map(RolePOJO::generate).toList();
        List<ReactionPOJO> reactions = guild.retrieveEmojis().complete().stream().map(ReactionPOJO::generate).toList();
        return new GuildPOJO(roles, ChannelViewPOJO.generate(guild), reactions);
    }
}
