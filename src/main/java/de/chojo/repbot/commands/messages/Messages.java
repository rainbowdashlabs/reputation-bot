package de.chojo.repbot.commands.messages;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.messages.handler.States;
import de.chojo.repbot.dao.provider.Guilds;

public class Messages extends SlashCommand {
    public Messages(Guilds guilds) {
        super(Slash.of("messages", "command.messages.description")
                .adminCommand()
                .subCommand(SubCommand.of("states", "command.messages.states.description")
                        .handler(new States(guilds)))
        );
    }
}
