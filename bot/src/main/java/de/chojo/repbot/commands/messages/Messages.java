/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.messages;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.messages.handler.States;
import de.chojo.repbot.dao.provider.GuildRepository;

public class Messages extends SlashCommand {
    public Messages(GuildRepository guildRepository) {
        super(Slash.of("messages", "command.messages.description")
                .guildOnly()
                .adminCommand()
                .subCommand(SubCommand.of("states", "command.messages.states.description")
                        .handler(new States(guildRepository))));
    }
}
