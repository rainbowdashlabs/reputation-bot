/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.gdpr;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.gdpr.handler.Delete;
import de.chojo.repbot.commands.gdpr.handler.Request;
import de.chojo.repbot.dao.provider.UserRepository;

public class Gdpr extends SlashCommand {

    public Gdpr(de.chojo.repbot.dao.access.Gdpr gdpr, UserRepository userRepository) {
        super(Slash.of("gdpr", "command.gdpr.description")
                .subCommand(SubCommand.of("request", "command.gdpr.request.description")
                        .handler(new Request(gdpr)))
                .subCommand(SubCommand.of("delete", "command.gdpr.delete.description")
                        .handler(new Delete(gdpr, userRepository))));
    }
}
