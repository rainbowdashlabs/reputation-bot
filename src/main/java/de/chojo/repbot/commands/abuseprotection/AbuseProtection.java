/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.abuseprotection;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.settings.DashboardLink;
import de.chojo.repbot.commands.settings.DashboardSection;
import de.chojo.repbot.config.Configuration;

/**
 * Former settings command. Links to the dashboard sections that hold its settings.
 */
public class AbuseProtection extends SlashCommand {
    public AbuseProtection(Configuration configuration) {
        super(Slash.of("abuseprotection", "command.abuseprotection.description")
                .guildOnly()
                .adminCommand()
                .command(new DashboardLink(configuration, DashboardSection.ABUSE_PROTECTION)));
    }
}
