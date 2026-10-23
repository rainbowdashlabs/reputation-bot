/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.messages;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.settings.DashboardLink;
import de.chojo.repbot.commands.settings.DashboardSection;
import de.chojo.repbot.config.Configuration;

/**
 * Former settings command. Links to the dashboard sections that hold its settings.
 */
public class Messages extends SlashCommand {
    public Messages(Configuration configuration) {
        super(Slash.of("messages", "command.messages.description")
                .guildOnly()
                .adminCommand()
                .command(new DashboardLink(configuration, DashboardSection.REPUTATION)));
    }
}
