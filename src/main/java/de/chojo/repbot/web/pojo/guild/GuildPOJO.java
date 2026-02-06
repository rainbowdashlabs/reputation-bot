/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.guild;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public class GuildPOJO {
    GuildMetaPOJO meta;
    List<RolePOJO> roles;
    ChannelViewPOJO channels;
    List<ReactionPOJO> reactions;
    List<MemberPOJO> integrations;

    public GuildPOJO(GuildMetaPOJO meta, List<RolePOJO> roles, ChannelViewPOJO channels, List<ReactionPOJO> reactions, List<MemberPOJO> integrations) {
        this.meta = meta;
        this.roles = roles;
        this.channels = channels;
        this.reactions = reactions;
        this.integrations = integrations;
    }

    public static GuildPOJO generate(Guild guild) {
        GuildMetaPOJO meta = GuildMetaPOJO.generate(guild);
        List<RolePOJO> roles = guild.getRoles().stream()
                .filter(r -> !r.isPublicRole())
                .filter(r -> !r.isManaged())
                .map(RolePOJO::generate)
                .toList();
        List<ReactionPOJO> reactions = guild.retrieveEmojis().complete().stream()
                .map(ReactionPOJO::generate)
                .toList();
        List<MemberPOJO> integrations = guild.getRoles().stream().filter(Role::isManaged)
                                     .map(e -> guild.findMembersWithRoles(e).get())
                                     .flatMap(List::stream)
                                     .distinct()
                                     .map(MemberPOJO::generate)
                                     .toList();
        return new GuildPOJO(meta, roles, ChannelViewPOJO.generate(guild), reactions, integrations);
    }
}
