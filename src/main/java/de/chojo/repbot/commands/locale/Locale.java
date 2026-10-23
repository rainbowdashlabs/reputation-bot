/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.locale;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.settings.DashboardLink;
import de.chojo.repbot.commands.settings.DashboardSection;
import de.chojo.repbot.config.Configuration;

/**
 * Former settings command. Links to the dashboard sections that hold its settings.
 */
public class Locale extends SlashCommand {
    public Locale(Configuration configuration) {
        super(Slash.of("locale", "command.locale.description")
                .guildOnly()
                .adminCommand()
                .command(new DashboardLink(configuration, DashboardSection.GENERAL)));
    }
}
