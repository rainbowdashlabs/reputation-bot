package de.chojo.repbot.commands.reactions;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.reactions.handler.Add;
import de.chojo.repbot.commands.reactions.handler.Info;
import de.chojo.repbot.commands.reactions.handler.Main;
import de.chojo.repbot.commands.reactions.handler.Remove;
import de.chojo.repbot.dao.provider.Guilds;

public class Reactions extends SlashCommand {
    public Reactions(Guilds guilds) {
        super(Slash.of("reactions", "command.reactions.description")
                .guildOnly()
                .adminCommand()
                .subCommand(SubCommand.of("main", "command.reactions.main.description")
                        .handler(new Main(guilds))
                        .argument(Argument.text("emote", "command.reactions.main.options.emote.description").asRequired()))
                .subCommand(SubCommand.of("add", "command.reactions.add.description")
                        .handler(new Add(guilds))
                        .argument(Argument.text("emote", "command.reactions.add.options.emote.description").asRequired()))
                .subCommand(SubCommand.of("remove", "command.reactions.remove.description")
                        .handler(new Remove(guilds))
                        .argument(Argument.text("emote", "command.reactions.remove.options.emote.description")
                                          .withAutoComplete().asRequired()))
                .subCommand(SubCommand.of("info", "command.reactions.info.description")
                        .handler(new Info(guilds)))
        );
    }
}
