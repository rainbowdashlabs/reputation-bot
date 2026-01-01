/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.reputation;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.repbot.commands.reputation.handler.Give;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.reputation.ReputationService;

import static de.chojo.jdautil.interactions.slash.Argument.user;

public class Reputation implements SlashProvider<Slash> {
    private final GuildRepository guildRepository;
    private ReputationService reputationService;

    public Reputation(GuildRepository guildRepository, ReputationService reputationService) {
        this.guildRepository  = guildRepository;
        this.reputationService = reputationService;
    }


    @Override
    public Slash slash() {
        return Slash.of("reputation", "command.reputation.description")
                .privateCommand()
                .command(new Give(guildRepository, reputationService))
                .argument(user("user", "command.reputation.options.user.description").asRequired())
                .build();
    }
}
