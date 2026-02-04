/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.guild;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.util.Comparator;

public class GuildMetaPOJO {
    private final RolePOJO highestBotRole;
    private final String name;
    private final String id;
    private final String iconUrl;

    public GuildMetaPOJO(RolePOJO highestBotRole, String name, String id, String iconUrl) {
        this.highestBotRole = highestBotRole;
        this.name = name;
        this.id = id;
        this.iconUrl = iconUrl;
    }

    public static GuildMetaPOJO generate(Guild guild) {
        var selfMember = guild.getSelfMember();
        var highestRole = selfMember.getRoles().stream()
                .max(Comparator.comparingInt(Role::getPosition))
                .orElse(null);
        RolePOJO highestBotRole = highestRole != null ? RolePOJO.generate(highestRole) : null;
        return new GuildMetaPOJO(highestBotRole, guild.getName(), guild.getId(), guild.getIconUrl());
    }
}
