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
        super(Slash.of("reactions", "command.reaction.description")
                .guildOnly()
                .adminCommand()
                .subCommand(SubCommand.of("main", "command.reaction.main.description")
                        .handler(new Main(guilds))
                        .argument(Argument.text("emote", "command.reaction.main.emote.description").asRequired()))
                .subCommand(SubCommand.of("add", "command.reaction.add.description")
                        .handler(new Add(guilds))
                        .argument(Argument.text("emote", "command.reaction.add.emote.description").asRequired()))
                .subCommand(SubCommand.of("remove", "command.reaction.remove.description")
                        .handler(new Remove(guilds))
                        .argument(Argument.text("emote", "command.reaction.remove.emote.description").withAutoComplete().asRequired()))
                .subCommand(SubCommand.of("info", "command.reaction.info.description")
                        .handler(new Info(guilds)))
        );
    }
}
