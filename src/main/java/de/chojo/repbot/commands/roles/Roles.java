/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.roles;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.settings.DashboardLink;
import de.chojo.repbot.commands.settings.DashboardSection;
import de.chojo.repbot.config.Configuration;
import net.dv8tion.jda.api.Permission;

/**
 * Former settings command. Links to the dashboard sections that hold its settings.
 */
public class Roles extends SlashCommand {
    public Roles(Configuration configuration) {
        super(Slash.of("roles", "command.roles.description")
                .guildOnly()
                .withPermission(Permission.MANAGE_ROLES)
                .command(new DashboardLink(configuration, DashboardSection.RANKS, DashboardSection.ROLES)));
    }
}
