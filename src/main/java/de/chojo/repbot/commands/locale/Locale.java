/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.locale;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.locale.handler.List;
import de.chojo.repbot.commands.locale.handler.Reset;
import de.chojo.repbot.commands.locale.handler.Set;
import de.chojo.repbot.dao.provider.GuildRepository;

public class Locale extends SlashCommand {
    public Locale(GuildRepository guildRepository) {
        super(Slash.of("locale", "command.locale.description")
                .guildOnly()
                .adminCommand()
                .subCommand(SubCommand.of("set", "command.locale.set.description")
                        .handler(new Set(guildRepository))
                        .argument(Argument.text("language", "command.locale.set.options.language.description")
                                .asRequired()
                                .withAutoComplete()))
                .subCommand(SubCommand.of("reset", "command.locale.reset.description")
                        .handler(new Reset(guildRepository)))
                .subCommand(
                        SubCommand.of("list", "command.locale.list.description").handler(new List())));
    }
}
