/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.validation;

import de.chojo.repbot.web.error.InvalidCategoryException;
import de.chojo.repbot.web.error.InvalidRoleException;
import de.chojo.repbot.dao.access.guildsession.GuildSession;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

public class GuildValidator {
    private final GuildSession guildSession;

    public GuildValidator(GuildSession guildSession) {
        this.guildSession = guildSession;
    }

    public void validateRoleIds(long roleId) {
        Role role = guildSession.repGuild().guild().getRoleById(roleId);
        if (role == null) {
            throw new InvalidRoleException(roleId);
        }
    }

    public void validateChannelIds(long channelId) {
        GuildChannel channel = guildSession.repGuild().guild().getGuildChannelById(channelId);
        if (channel == null) {
            throw new InvalidCategoryException(channelId);
        }
    }

    public void validateCategoryIds(long categoryId) {
        GuildChannel category = guildSession.repGuild().guild().getCategoryById(categoryId);
        if (category == null) {
            throw new InvalidCategoryException(categoryId);
        }
    }
}
