/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.supporter;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.supporter.handler.Activate;
import de.chojo.repbot.commands.supporter.handler.Info;
import de.chojo.repbot.commands.supporter.handler.Refresh;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.PremiumService;

import static de.chojo.jdautil.interactions.slash.Argument.text;
import static de.chojo.jdautil.interactions.slash.SubCommand.sub;

public class Supporter extends SlashCommand {
    public Supporter(PremiumService premiumService, Configuration configuration, GuildRepository guildRepository) {
        super(Slash.of("supporter", "command.supporter.description")
                   .adminCommand()
                   .guildOnly()
                   .subCommand(sub("refresh", "command.supporter.refresh.description")
                           .handler(new Refresh(premiumService)))
                   .subCommand(sub("activate", "command.supporter.activate.description")
                           .handler(new Activate(premiumService, configuration))
                           .argument(text("coupon", "command.supporter.activate.options.coupon.description").asRequired().withAutoComplete()))
                   .subCommand(sub("info", "command.supporter.info.description")
                           .handler(new Info(premiumService, configuration, guildRepository)))
                   .build());
    }
}
