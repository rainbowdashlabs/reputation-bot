/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.guild;

import net.dv8tion.jda.api.entities.Guild;

import java.util.List;

public class GuildPOJO {
    List<RolePOJO> roles;
    ChannelViewPOJO channels;
    List<ReactionPOJO> reactions;
    List<MemberPOJO> integrations;

    public GuildPOJO(
            List<RolePOJO> roles,
            ChannelViewPOJO channels,
            List<ReactionPOJO> reactions,
            List<MemberPOJO> integrations) {
        this.roles = roles;
        this.channels = channels;
        this.reactions = reactions;
        this.integrations = integrations;
    }

    public static GuildPOJO generate(Guild guild) {
        List<RolePOJO> roles = guild.getRoles().stream()
                .filter(r -> !r.isPublicRole())
                .filter(r -> !r.isManaged())
                .map(RolePOJO::generate)
                .toList();
        List<ReactionPOJO> reactions = guild.retrieveEmojis().complete().stream()
                .map(ReactionPOJO::generate)
                .toList();
        List<MemberPOJO> integrations = guild.getMemberCache()
                .applyStream(stream -> stream.filter(member -> member.getUser().isBot())
                        .map(MemberPOJO::generate)
                        .toList());
        return new GuildPOJO(roles, ChannelViewPOJO.generate(guild), reactions, integrations);
    }
}
