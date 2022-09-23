package de.chojo.repbot.commands.locale;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.locale.handler.List;
import de.chojo.repbot.commands.locale.handler.Reset;
import de.chojo.repbot.commands.locale.handler.Set;
import de.chojo.repbot.dao.provider.Guilds;

public class Locale extends SlashCommand {
    public Locale(Guilds guilds) {
        super(Slash.of("locale", "command.locale.description")
                .guildOnly()
                .adminCommand()
                .subCommand(SubCommand.of("set", "command.locale.set.description")
                        .handler(new Set(guilds))
                        .argument(Argument.text("language", "command.locale.set.language.description").asRequired()
                                          .withAutoComplete()))
                .subCommand(SubCommand.of("reset", "command.locale.reset.description")
                        .handler(new Reset(guilds)))
                .subCommand(SubCommand.of("list", "command.locale.list.description")
                        .handler(new List()))
        );
    }
}
