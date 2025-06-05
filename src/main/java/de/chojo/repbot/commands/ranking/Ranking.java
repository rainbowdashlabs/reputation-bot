/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.ranking;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.ranking.handler.guild.GuildGiven;
import de.chojo.repbot.commands.ranking.handler.guild.GuildReceived;
import de.chojo.repbot.commands.ranking.handler.user.UserGiven;
import de.chojo.repbot.commands.ranking.handler.user.UserReceived;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;

import static de.chojo.jdautil.interactions.slash.Argument.text;
import static de.chojo.jdautil.interactions.slash.Argument.user;
import static de.chojo.jdautil.interactions.slash.Group.group;
import static de.chojo.jdautil.interactions.slash.SubCommand.sub;

public class Ranking extends SlashCommand {
    public Ranking(GuildRepository guildRepository, Configuration configuration) {
        super(Slash.of("ranking", "command.ranking.description")
                   .guildOnly()
                   .group(group("server", "command.ranking.server.description")
                           .subCommand(sub("received", "command.ranking.server.received.description")
                                   .handler(new GuildReceived(guildRepository))
                                   .argument(text("mode", "command.ranking.server.received.options.mode.description").withAutoComplete())
                           )
                           .subCommand(sub("given", "command.ranking.server.given.description")
                                   .handler(new GuildGiven(guildRepository))
                                   .argument(text("mode", "command.ranking.server.given.options.mode.description").withAutoComplete())
                           )
                   )
                   .group(group("user", "command.ranking.user.description")
                           .subCommand(sub("received", "command.ranking.user.received.description")
                                   .handler(new UserReceived(guildRepository))
                                   .argument(text("mode", "command.ranking.user.received.options.mode.description").withAutoComplete())
                                   .argument(user("user", "command.ranking.user.received.options.user.description"))
                           )
                           .subCommand(sub("given", "command.ranking.user.given.description")
                                   .handler(new UserGiven(guildRepository))
                                   .argument(text("mode", "command.ranking.user.given.options.mode.description").withAutoComplete())
                                   .argument(user("user", "command.ranking.user.given.options.user.description"))
                           )
                   )
        );
    }
}
