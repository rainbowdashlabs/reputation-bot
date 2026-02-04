/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.channel.handler.Add;
import de.chojo.repbot.commands.channel.handler.List;
import de.chojo.repbot.commands.channel.handler.ListType;
import de.chojo.repbot.commands.channel.handler.Remove;
import de.chojo.repbot.commands.channel.handler.Set;
import de.chojo.repbot.commands.channel.handler.announcement.AnnouncementInfo;
import de.chojo.repbot.commands.channel.handler.announcement.AnnouncementLocation;
import de.chojo.repbot.commands.channel.handler.announcement.AnnouncementState;
import de.chojo.repbot.commands.channel.handler.announcement.AnnouncementWhere;
import de.chojo.repbot.commands.channel.handler.autopost.AutopostDisable;
import de.chojo.repbot.commands.channel.handler.autopost.AutopostEnable;
import de.chojo.repbot.commands.channel.handler.autopost.AutopostInfo;
import de.chojo.repbot.commands.channel.handler.autopost.AutopostRefresh;
import de.chojo.repbot.commands.channel.handler.log.LogDisable;
import de.chojo.repbot.commands.channel.handler.log.LogEnable;
import de.chojo.repbot.commands.channel.handler.systemchannel.SystemChannelDisable;
import de.chojo.repbot.commands.channel.handler.systemchannel.SystemChannelEnable;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.AutopostService;

import static de.chojo.jdautil.interactions.slash.Argument.bool;
import static de.chojo.jdautil.interactions.slash.Argument.channel;
import static de.chojo.jdautil.interactions.slash.Argument.text;
import static de.chojo.jdautil.interactions.slash.Group.group;
import static de.chojo.jdautil.interactions.slash.SubCommand.sub;

public class Channel extends SlashCommand {
    public Channel(GuildRepository guildRepository, Configuration configuration, AutopostService autopostService) {
        super(Slash.of("channel", "command.channel.description")
                   .adminCommand()
                   .guildOnly()
                   .subCommand(sub("set", "command.channel.set.description")
                           .handler(new Set(guildRepository))
                           .argument(channel("channel", "command.channel.set.options.channel.description").asRequired()))
                   .subCommand(sub("add", "command.channel.add.description")
                           .handler(new Add(guildRepository, configuration))
                           .argument(channel("channel", "command.channel.add.options.channel.description").asRequired()))
                   .subCommand(sub("remove", "command.channel.remove.description")
                           .handler(new Remove(guildRepository))
                           .argument(channel("channel", "command.channel.remove.options.channel.description")
                                   .asRequired()))
                   .subCommand(sub("listtype", "command.channel.listtype.description")
                           .handler(new ListType(guildRepository, configuration))
                           .argument(text("type", "command.channel.listtype.options.type.description").asRequired()
                                                                                                      .withAutoComplete()))
                   .subCommand(sub("list", "command.channel.list.description")
                           .handler(new List(guildRepository)))
                   .group(group("announcement", "command.channel.announcement.description")
                           .subCommand(sub("state", "command.channel.announcement.state.description")
                                   .handler(new AnnouncementState(guildRepository))
                                   .argument(bool("active", "command.channel.announcement.state.options.active.description").asRequired()))
                           .subCommand(sub("where", "command.channel.announcement.where.description")
                                   .handler(new AnnouncementWhere(guildRepository))
                                   .argument(text("where", "command.channel.announcement.where.options.where.description").asRequired().withAutoComplete()))
                           .subCommand(sub("channel", "command.channel.announcement.channel.description")
                                   .handler(new AnnouncementLocation(guildRepository))
                                   .argument(channel("channel", "command.channel.announcement.channel.options.channel.description").asRequired()))
                           .subCommand(sub("info", "command.channel.announcement.info.description")
                                   .handler(new AnnouncementInfo(guildRepository))))
                   .group(group("autopost", "command.channel.autopost.description")
                           .subCommand(sub("enable", "command.channel.autopost.enable.description")
                                   .handler(new AutopostEnable(guildRepository, configuration, autopostService))
                                   .argument(channel("channel", "command.channel.autopost.enable.options.channel.description").asRequired())
                                   .argument(text("refreshtype", "command.channel.autopost.enable.options.refreshtype.description").withAutoComplete())
                                   .argument(text("refreshinterval", "command.channel.autopost.enable.options.refreshinterval.description").withAutoComplete()))
                           .subCommand(sub("disable", "command.channel.autopost.disable.description")
                                   .handler(new AutopostDisable(guildRepository, autopostService)))
                           .subCommand(sub("info", "command.channel.autopost.info.description")
                                   .handler(new AutopostInfo(guildRepository)))
                           .subCommand(sub("refresh", "command.channel.autopost.refresh.description")
                                   .handler(new AutopostRefresh(autopostService, guildRepository))))
                   .group(group("log", "command.channel.log.description")
                           .subCommand(sub("enable", "command.channel.log.enable.description")
                                   .handler(new LogEnable(guildRepository, configuration))
                                   .argument(channel("channel", "command.channel.log.enable.options.channel.description").asRequired()))
                           .subCommand(sub("disable", "command.channel.log.disable.description")
                                   .handler(new LogDisable(guildRepository))))
                   .group(group("systemchannel", "command.channel.systemchannel.description")
                           .subCommand(sub("enable", "command.channel.systemchannel.enable.description")
                                   .handler(new SystemChannelEnable(guildRepository))
                                   .argument(channel("channel", "command.channel.systemchannel.enable.options.channel.description").asRequired()))
                           .subCommand(sub("disable", "command.channel.systemchannel.disable.description")
                                   .handler(new SystemChannelDisable(guildRepository))))
        );
    }
}
