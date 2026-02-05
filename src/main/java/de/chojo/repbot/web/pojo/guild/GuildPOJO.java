/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.guild;

import net.dv8tion.jda.api.entities.Guild;

import java.util.List;

public class GuildPOJO {
    GuildMetaPOJO meta;
    List<RolePOJO> roles;
    ChannelViewPOJO channels;
    List<ReactionPOJO> reactions;

    public GuildPOJO(GuildMetaPOJO meta, List<RolePOJO> roles, ChannelViewPOJO channels, List<ReactionPOJO> reactions) {
        this.meta = meta;
        this.roles = roles;
        this.channels = channels;
        this.reactions = reactions;
    }

    public static GuildPOJO generate(Guild guild) {
        GuildMetaPOJO meta = GuildMetaPOJO.generate(guild);
        List<RolePOJO> roles = guild.getRoles().stream()
                .filter(r -> !r.isPublicRole())
                .map(RolePOJO::generate)
                .toList();
        List<ReactionPOJO> reactions = guild.retrieveEmojis().complete().stream()
                .map(ReactionPOJO::generate)
                .toList();
        return new GuildPOJO(meta, roles, ChannelViewPOJO.generate(guild), reactions);
    }
}
