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
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.RoleAssigner;

public class Reputation extends SlashCommand {
    public Reputation(GuildRepository guildRepository, Configuration configuration, RoleAssigner roleAssigner) {
        super(Slash.of("rep", "command.rep.description")
                .guildOnly()
                .command(new Profile(guildRepository, configuration, roleAssigner))
                .argument(Argument.user("user", "command.rep.options.user.description"))
        );
    }
}
