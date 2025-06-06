/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.abuseprotection;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.abuseprotection.handler.limit.Cooldown;
import de.chojo.repbot.commands.abuseprotection.handler.context.DonorContext;
import de.chojo.repbot.commands.abuseprotection.handler.limit.DonorLimit;
import de.chojo.repbot.commands.abuseprotection.handler.Info;
import de.chojo.repbot.commands.abuseprotection.handler.message.MaxMessageAge;
import de.chojo.repbot.commands.abuseprotection.handler.message.MaxMessageReputation;
import de.chojo.repbot.commands.abuseprotection.handler.message.MinMessages;
import de.chojo.repbot.commands.abuseprotection.handler.context.ReceiverContext;
import de.chojo.repbot.commands.abuseprotection.handler.limit.ReceiverLimit;
import de.chojo.repbot.dao.provider.GuildRepository;

import java.time.temporal.ChronoUnit;

public class AbuseProtection extends SlashCommand {
    public AbuseProtection(GuildRepository guildRepository) {
        super(Slash.of("abuseprotection", "command.abuseprotection.description")
                .adminCommand()
                .guildOnly()
                .subCommand(SubCommand.of("info", "command.abuseprotection.info.description")
                        .handler(new Info(guildRepository)))
                .group(Group.of("message", "command.abuseprotection.message.description")
                        .subCommand(SubCommand.of("age", "command.abuseprotection.message.age.description")
                                .handler(new MaxMessageAge(guildRepository))
                                .argument(Argument.integer("minutes", "command.abuseprotection.message.age.options.minutes.description")
                                                  .min(0)
                                                  .max(ChronoUnit.WEEKS.getDuration().toMinutes())))
                        .subCommand(SubCommand.of("reputation", "command.abuseprotection.message.reputation.description")
                                .handler(new MaxMessageReputation(guildRepository))
                                .argument(Argument.integer("amount", "command.abuseprotection.message.reputation.options.amount.description")
                                                  .min(1)
                                                  // The max amount of components without delete button
                                                  .max(49)))
                        .subCommand(SubCommand.of("min", "command.abuseprotection.message.min.description")
                                .handler(new MinMessages(guildRepository))
                                .argument(Argument.integer("messages", "command.abuseprotection.message.min.options.messages.description")
                                                  .min(0)
                                                  // The max amount of retrieved history
                                                  .max(100))))
                .group(Group.of("context", "command.abuseprotection.context.description")
                        .subCommand(SubCommand.of("donor", "command.abuseprotection.context.donor.description")
                                .handler(new DonorContext(guildRepository))
                                .argument(Argument.bool("state", "command.abuseprotection.context.donor.options.state.description")))
                        .subCommand(SubCommand.of("receiver", "command.abuseprotection.context.receiver.description")
                                .handler(new ReceiverContext(guildRepository))
                                .argument(Argument.bool("state", "command.abuseprotection.context.receiver.options.state.description"))))
                .group(Group.of("limit", "command.abuseprotection.limit.description")
                        .subCommand(SubCommand.of("cooldown", "command.abuseprotection.limit.cooldown.description")
                                .handler(new Cooldown(guildRepository))
                                .argument(Argument.integer("minutes", "command.abuseprotection.limit.cooldown.options.minutes.description")
                                                  .min(0)
                                                  .max(ChronoUnit.WEEKS.getDuration().toMinutes())))
                        .subCommand(SubCommand.of("donor", "command.abuseprotection.limit.donor.description")
                                .handler(new DonorLimit(guildRepository))
                                .argument(Argument.integer("limit", "command.abuseprotection.limit.donor.options.limit.description")
                                                  .min(0)
                                                  .max(100_000))
                                .argument(Argument.integer("hours", "command.abuseprotection.limit.donor.options.hours.description")
                                                  .min(1)
                                                  .max(ChronoUnit.WEEKS.getDuration().toHours())))
                        .subCommand(SubCommand.of("receiver", "command.abuseprotection.limit.receiver.description")
                                .handler(new ReceiverLimit(guildRepository))
                                .argument(Argument.integer("limit", "command.abuseprotection.limit.receiver.options.limit.description")
                                                  .min(0)
                                                  .max(100_000))
                                .argument(Argument.integer("hours", "command.abuseprotection.limit.receiver.options.hours.description")
                                                  .min(1)
                                                  .max(ChronoUnit.WEEKS.getDuration().toHours()))))
        );
    }
}
