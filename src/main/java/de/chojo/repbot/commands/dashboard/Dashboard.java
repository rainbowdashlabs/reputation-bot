/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.dashboard;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.dashboard.handler.Show;
import de.chojo.repbot.dao.provider.Guilds;

public class Dashboard extends SlashCommand {
    public Dashboard(Guilds guilds) {
        super(Slash.of("dashboard", "command.dashboard.description")
                .guildOnly()
                .command(new Show(guilds)));
    }
}
