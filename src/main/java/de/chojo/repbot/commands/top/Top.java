/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.top;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.top.handler.Show;
import de.chojo.repbot.dao.provider.GuildRepository;

public class Top extends SlashCommand {
    public Top(GuildRepository guildRepository) {
        super(Slash.of("top", "command.top.description")
                .guildOnly()
                .command(new Show(guildRepository))
                .argument(Argument.text("mode", "command.top.options.mode.description").withAutoComplete()));
    }
}
