/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.prune;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.prune.handler.Guild;
import de.chojo.repbot.commands.prune.handler.User;
import de.chojo.repbot.service.GdprService;
import net.dv8tion.jda.api.Permission;

/**
 * Represents the Prune command which allows pruning of users or guilds.
 */
public class Prune extends SlashCommand {

    /**
     * Constructs a Prune command with the specified GDPR service.
     *
     * @param service the GDPR service to handle pruning operations
     */
    public Prune(GdprService service) {
        super(Slash.of("prune", "command.prune.description")
                .guildOnly()
                .withPermission(Permission.MESSAGE_MANAGE)
                .subCommand(SubCommand.of("user", "command.prune.user.description")
                        .handler(new User(service))
                        .argument(Argument.user("user", "command.prune.user.options.user.description"))
                        .argument(Argument.text("userid", "command.prune.user.options.userid.description")))
                .subCommand(SubCommand.of("guild", "command.prune.guild.description")
                        .handler(new Guild(service)))
        );
    }
}
