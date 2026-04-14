/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.guild;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.util.Comparator;
import java.util.List;

import static java.util.Collections.emptyList;

public class GuildPOJO {
    public static final GuildPOJO UNKNOWN = new GuildPOJO(RolePOJO.UNKNOWN,"Unknown", "0", "", emptyList(), ChannelViewPOJO.EMPTY, emptyList(), emptyList());
    private final RolePOJO highestBotRole;
    private final String name;
    private final String id;
    private final String iconUrl;
    private final List<RolePOJO> roles;
    private final ChannelViewPOJO channels;
    private final List<ReactionPOJO> reactions;
    private final List<MemberPOJO> integrations;

    public GuildPOJO(
            RolePOJO highestBotRole,
            String name,
            String id,
            String iconUrl,
            List<RolePOJO> roles,
            ChannelViewPOJO channels,
            List<ReactionPOJO> reactions,
            List<MemberPOJO> integrations) {
        this.highestBotRole = highestBotRole;
        this.name = name;
        this.id = id;
        this.iconUrl = iconUrl;
        this.roles = roles;
        this.channels = channels;
        this.reactions = reactions;
        this.integrations = integrations;
    }

    public static GuildPOJO generate(Guild guild) {
        var selfMember = guild.getSelfMember();
        var highestRole = selfMember.getRoles().stream()
                .max(Comparator.comparingInt(Role::getPosition))
                .orElse(null);
        RolePOJO highestBotRole = highestRole != null ? RolePOJO.generate(highestRole) : null;

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
        return new GuildPOJO(
                highestBotRole,
                guild.getName(),
                guild.getId(),
                guild.getIconUrl(),
                roles,
                ChannelViewPOJO.generate(guild),
                reactions,
                integrations);
    }
}
