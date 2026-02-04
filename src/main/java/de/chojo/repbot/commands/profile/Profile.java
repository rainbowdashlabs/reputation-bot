/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.profile;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.RoleAssigner;

public class Profile extends SlashCommand {
    public Profile(GuildRepository guildRepository, Configuration configuration, RoleAssigner roleAssigner) {
        super(Slash.of("profile", "command.profile.description")
                   .guildOnly()
                   .command(new de.chojo.repbot.commands.profile.handler.Profile(guildRepository, configuration, roleAssigner))
                   .argument(Argument.user("user", "command.profile.options.user.description"))
                   .argument(Argument.bool("detailed", "command.profile.options.detailed.description"))
        );
    }
}
