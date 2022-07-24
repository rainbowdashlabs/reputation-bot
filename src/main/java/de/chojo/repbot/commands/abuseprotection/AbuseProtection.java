package de.chojo.repbot.commands.abuseprotection;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.abuseprotection.handler.Cooldown;
import de.chojo.repbot.commands.abuseprotection.handler.DonorContext;
import de.chojo.repbot.commands.abuseprotection.handler.DonorLimit;
import de.chojo.repbot.commands.abuseprotection.handler.Info;
import de.chojo.repbot.commands.abuseprotection.handler.MaxMessageAge;
import de.chojo.repbot.commands.abuseprotection.handler.MaxMessageReputation;
import de.chojo.repbot.commands.abuseprotection.handler.MinMessages;
import de.chojo.repbot.commands.abuseprotection.handler.ReceiverContext;
import de.chojo.repbot.commands.abuseprotection.handler.ReceiverLimit;
import de.chojo.repbot.dao.provider.Guilds;

public class AbuseProtection extends SlashCommand {
    public AbuseProtection(Guilds guilds) {
        super(Slash.of("abuseprotection", "command.abuseProtection.description")
                .adminCommand()
                .subCommand(SubCommand.of("info", "command.abuseProtection.sub.info")
                        .handler(new Info(guilds)))
                .subCommand(SubCommand.of("maxmessageage", "command.abuseProtection.sub.maxMessageAge")
                        .handler(new MaxMessageAge(guilds))
                        .argument(Argument.integer("minutes", "command.abuseProtection.sub.maxMessageAge.arg.minutes")))
                .subCommand(SubCommand.of("maxmessagereputation", "command.abuseProtection.sub.maxMessageRep")
                        .handler(new MaxMessageReputation(guilds))
                        .argument(Argument.integer("amount", "command.abuseProtection.sub.maxMessageRep.arg.amount")))
                .subCommand(SubCommand.of("minmessages", "command.abuseProtection.sub.minMessages")
                        .handler(new MinMessages(guilds))
                        .argument(Argument.integer("messages", "command.abuseProtection.sub.minMessages.arg.messages")))
                .subCommand(SubCommand.of("cooldown", "command.abuseProtection.sub.cooldown")
                        .handler(new Cooldown(guilds))
                        .argument(Argument.integer("minutes", "command.abuseProtection.sub.cooldown.arg.minutes")))
                .subCommand(SubCommand.of("donorcontext", "command.abuseProtection.sub.donorContext")
                        .handler(new DonorContext(guilds))
                        .argument(Argument.bool("state", "command.abuseProtection.sub.donorContext.arg.state")))
                .subCommand(SubCommand.of("receivercontext", "command.abuseProtection.sub.receiverContext")
                        .handler(new ReceiverContext(guilds))
                        .argument(Argument.bool("state", "command.abuseProtection.sub.receiverContext.arg.state")))
                .subCommand(SubCommand.of("donorlimit", "command.abuseProtection.sub.donorLimit")
                        .handler(new DonorLimit(guilds))
                        .argument(Argument.integer("limit", "command.abuseProtection.sub.donorLimit.arg.limit"))
                        .argument(Argument.integer("hours", "command.abuseProtection.sub.donorLimit.arg.hours")))
                .subCommand(SubCommand.of("receiverlimit", "command.abuseProtection.sub.receiverLimit")
                        .handler(new ReceiverLimit(guilds))
                        .argument(Argument.integer("limit", "command.abuseProtection.sub.receiverLimit.arg.limit"))
                        .argument(Argument.integer("hours", "command.abuseProtection.sub.receiverLimit.arg.hours")))
        );
    }
}
