/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.reactions;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.reactions.handler.Add;
import de.chojo.repbot.commands.reactions.handler.Info;
import de.chojo.repbot.commands.reactions.handler.Main;
import de.chojo.repbot.commands.reactions.handler.Remove;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;

public class Reactions extends SlashCommand {
    public Reactions(GuildRepository guildRepository, Configuration configuration) {
        super(Slash.of("reactions", "command.reactions.description")
                .guildOnly()
                .adminCommand()
                .subCommand(SubCommand.of("main", "command.reactions.main.description")
                        .handler(new Main(guildRepository))
                        .argument(Argument.text("emote", "command.reactions.main.options.emote.description")
                                .asRequired()))
                .subCommand(SubCommand.of("add", "command.reactions.add.description")
                        .handler(new Add(guildRepository, configuration))
                        .argument(Argument.text("emote", "command.reactions.add.options.emote.description")
                                .asRequired()))
                .subCommand(SubCommand.of("remove", "command.reactions.remove.description")
                        .handler(new Remove(guildRepository))
                        .argument(Argument.text("emote", "command.reactions.remove.options.emote.description")
                                .withAutoComplete()
                                .asRequired()))
                .subCommand(SubCommand.of("info", "command.reactions.info.description")
                        .handler(new Info(guildRepository, configuration))));
    }
}
