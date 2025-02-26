/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.invite;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.invite.handler.Show;
import de.chojo.repbot.config.Configuration;

/**
 * Command class for the invite command.
 */
public class Invite extends SlashCommand {

    /**
     * Constructs a new Invite command.
     *
     * @param configuration the configuration of the application
     */
    public Invite(Configuration configuration) {
        super(Slash.of("invite", "command.invite.description")
                .command(new Show(configuration)));
    }
}
