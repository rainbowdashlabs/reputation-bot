/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.debug;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.debug.handler.Show;
import de.chojo.repbot.dao.provider.Guilds;

public class Debug extends SlashCommand {

    public Debug(Guilds guilds) {
        super(Slash.of("debug", "command.debug.description")
                .guildOnly()
                .adminCommand()
                .command(new Show(guilds)));
    }
}
