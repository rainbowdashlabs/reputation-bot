/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.reputation;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.reputation.handler.Profile;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.service.RoleAssigner;

/**
 * Represents the reputation command for the bot.
 */
public class Reputation extends SlashCommand {
    /**
     * Constructs a Reputation command with the specified guilds provider, configuration, and role assigner.
     *
     * @param guilds the guilds provider
     * @param configuration the configuration object
     * @param roleAssigner the role assigner service
     */
    public Reputation(Guilds guilds, Configuration configuration, RoleAssigner roleAssigner) {
        super(Slash.of("rep", "command.rep.description")
                .guildOnly()
                .command(new Profile(guilds, configuration, roleAssigner))
                .argument(Argument.user("user", "command.rep.options.user.description"))
        );
    }
}
