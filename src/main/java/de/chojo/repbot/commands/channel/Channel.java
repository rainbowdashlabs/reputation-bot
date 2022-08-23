package de.chojo.repbot.commands.channel;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.channel.handler.Add;
import de.chojo.repbot.commands.channel.handler.List;
import de.chojo.repbot.commands.channel.handler.ListType;
import de.chojo.repbot.commands.channel.handler.Remove;
import de.chojo.repbot.commands.channel.handler.Set;
import de.chojo.repbot.commands.channel.handler.announcement.Info;
import de.chojo.repbot.commands.channel.handler.announcement.Location;
import de.chojo.repbot.commands.channel.handler.announcement.State;
import de.chojo.repbot.dao.provider.Guilds;

public class Channel extends SlashCommand {
    public Channel(Guilds guilds) {
        super(Slash.of("channel", "command.channel.description")
                .adminCommand()
                .guildOnly()
                .subCommand(SubCommand.of("set", "command.channel.set.description")
                        .handler(new Set(guilds))
                        .argument(Argument.channel("channel", "command.channel.set.channel.description").asRequired()))
                .subCommand(SubCommand.of("add", "command.channel.add.description")
                        .handler(new Add(guilds))
                        .argument(Argument.channel("channel", "command.channel.add.channel.description").asRequired()))
                .subCommand(SubCommand.of("remove", "command.channel.remove.description")
                        .handler(new Remove(guilds))
                        .argument(Argument.channel("channel", "command.channel.remove.channel.description").asRequired()))
                .subCommand(SubCommand.of("listtype", "command.channel.listtype.description")
                        .handler(new ListType(guilds))
                        .argument(Argument.text("type", "command.channel.listtype.type.description").asRequired().withAutoComplete()))
                .subCommand(SubCommand.of("list", "command.channel.list.description")
                        .handler(new List(guilds)))
                .group(Group.of("announcement", "command.channel.announcement.description")
                        .subCommand(SubCommand.of("state", "command.channel.announcement.state.description")
                                .handler(new State(guilds))
                                .argument(Argument.bool("active", "command.channel.announcement.state.active.description")))
                        .subCommand(SubCommand.of("where", "command.channel.announcement.where.description")
                                .handler(new de.chojo.repbot.commands.channel.handler.announcement.Channel(guilds))
                                .argument(Argument.channel("where", "command.channel.announcement.where.where.description")))
                        .subCommand(SubCommand.of("channel", "command.channel.announcement.channel.description")
                                .handler(new Location(guilds))
                                .argument(Argument.channel("channel", "command.channel.announcement.channel.channel.description")))
                        .subCommand(SubCommand.of("info", "command.channel.announcement.info.description")
                                .handler(new Info(guilds))))
        );
    }
}
