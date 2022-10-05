package de.chojo.repbot.commands.log;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.log.handler.Analyzer;
import de.chojo.repbot.commands.log.handler.Donated;
import de.chojo.repbot.commands.log.handler.Message;
import de.chojo.repbot.commands.log.handler.Received;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.Permission;

public class Log extends SlashCommand {
    public Log(Guilds guilds) {
        super(Slash.of("log", "command.log.description")
                .guildOnly()
                .withPermission(Permission.MESSAGE_MANAGE)
                .subCommand(SubCommand.of("received", "command.log.received.description")
                        .handler(new Received(guilds))
                        .argument(Argument.user("user", "command.log.received.user.description").asRequired()))
                .subCommand(SubCommand.of("donated", "command.log.donated.description")
                        .handler(new Donated(guilds))
                        .argument(Argument.user("user", "command.log.donated.user.description").asRequired()))
                .subCommand(SubCommand.of("message", "command.log.message.description")
                        .handler(new Message(guilds))
                        .argument(Argument.text("messageid", "command.log.message.messageid.description").asRequired()))
                .subCommand(SubCommand.of("analyzer", "command.log.analyzer.description")
                        .handler(new Analyzer(guilds))
                        .argument(Argument.text("messageid", "command.log.analyzer.messageid.description").asRequired()))
        );
    }
}
