/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.log;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.log.handler.Analyzer;
import de.chojo.repbot.commands.log.handler.Donated;
import de.chojo.repbot.commands.log.handler.Message;
import de.chojo.repbot.commands.log.handler.Received;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.Permission;

public class Log extends SlashCommand {
    public Log(GuildRepository guildRepository) {
        super(Slash.of("log", "command.log.description")
                .guildOnly()
                .withPermission(Permission.MESSAGE_MANAGE)
                .subCommand(SubCommand.of("received", "command.log.received.description")
                        .handler(new Received(guildRepository))
                        .argument(Argument.user("user", "command.log.received.options.user.description").asRequired()))
                .subCommand(SubCommand.of("donated", "command.log.donated.description")
                        .handler(new Donated(guildRepository))
                        .argument(Argument.user("user", "command.log.donated.options.user.description").asRequired()))
                .subCommand(SubCommand.of("message", "command.log.message.description")
                        .handler(new Message(guildRepository))
                        .argument(Argument.text("messageid", "command.log.message.options.messageid.description").asRequired()))
                .subCommand(SubCommand.of("analyzer", "command.log.analyzer.description")
                        .handler(new Analyzer(guildRepository))
                        .argument(Argument.text("messageid", "command.log.analyzer.options.messageid.description").asRequired()))
        );
    }
}
