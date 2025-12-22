/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.rep;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.commands.rep.handler.Give;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.reputation.ReputationService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import static de.chojo.jdautil.interactions.slash.Argument.user;

public class Rep implements SlashProvider<Slash> {
    private final GuildRepository guildRepository;
    private ReputationService reputationService;

    public Rep(GuildRepository guildRepository, ReputationService reputationService) {
        this.guildRepository  = guildRepository;
        this.reputationService = reputationService;
    }


    @Override
    public Slash slash() {
        return Slash.of("rep", "commands.rep.description")
                .command(new Give(guildRepository, reputationService))
                .argument(user("user", "commands.rep.options.user.description"))
                .build();
    }
}
