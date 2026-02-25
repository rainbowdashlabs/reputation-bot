/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.setup;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.setup.handler.Start;
import de.chojo.repbot.web.services.SessionService;

public class Setup extends SlashCommand {
    public Setup(SessionService sessionService) {
        super(Slash.of("setup", "command.setup.description")
                .guildOnly()
                .adminCommand()
                .command(new Start(sessionService)));
    }
}
