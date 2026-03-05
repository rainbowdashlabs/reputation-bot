/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.vote;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.vote.handler.Show;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.VoteRepository;

public class Vote extends SlashCommand {
    public Vote(Configuration configuration, VoteRepository voteRepository) {
        super(Slash.of("vote", "command.vote.description")
                .command(new Show(configuration, voteRepository)));
    }
}
