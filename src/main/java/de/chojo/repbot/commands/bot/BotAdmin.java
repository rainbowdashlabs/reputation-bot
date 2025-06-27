/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.jdautil.pagination.PageService;
import de.chojo.repbot.commands.bot.handler.Debug;
import de.chojo.repbot.commands.bot.handler.InvalidateCache;
import de.chojo.repbot.commands.bot.handler.Leave;
import de.chojo.repbot.commands.bot.handler.Redeploy;
import de.chojo.repbot.commands.bot.handler.Sample;
import de.chojo.repbot.commands.bot.handler.Search;
import de.chojo.repbot.commands.bot.handler.SharedGuilds;
import de.chojo.repbot.commands.bot.handler.entitlement.Create;
import de.chojo.repbot.commands.bot.handler.entitlement.Delete;
import de.chojo.repbot.commands.bot.handler.entitlement.Show;
import de.chojo.repbot.commands.bot.handler.log.Analyzer;
import de.chojo.repbot.commands.bot.handler.system.Metrics;
import de.chojo.repbot.commands.bot.handler.system.Reload;
import de.chojo.repbot.commands.bot.handler.system.Restart;
import de.chojo.repbot.commands.bot.handler.system.Shudown;
import de.chojo.repbot.commands.bot.handler.system.Status;
import de.chojo.repbot.commands.bot.handler.system.Upgrade;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.statistic.Statistic;

import static de.chojo.jdautil.interactions.slash.Argument.text;
import static de.chojo.jdautil.interactions.slash.Group.group;
import static de.chojo.jdautil.interactions.slash.SubCommand.sub;

public class BotAdmin implements SlashProvider<Slash> {
    private final Slash slash;

    public BotAdmin(GuildRepository guildRepository, Configuration configuration, Statistic statistics) {
        var debug = new Debug(guildRepository);
        slash = Slash.of("bot", "Bot admin commands.")
                   .unlocalized()
                   .guildOnly()
                   .adminCommand()
                   .privateCommand()
                   .group(Group.of("system", "System management")
                               .subCommand(SubCommand.of("restart", "Restart bot")
                                                     .handler(new Restart(configuration)))
                               .subCommand(SubCommand.of("upgrade", "Deploy an update")
                                                     .handler(new Upgrade(configuration)))
                               .subCommand(SubCommand.of("shutdown", "Shutdown the bot.")
                                                     .handler(new Shudown(configuration)))
                               .subCommand(SubCommand.of("status", "System status")
                                                     .handler(new Status(statistics)))
                               .subCommand(SubCommand.of("metrics", "System metrics")
                                                     .handler(new Metrics(configuration)))
                               .subCommand(SubCommand.of("reload", "Reload configuration")
                                                     .handler(new Reload(configuration))))
                   .subCommand(SubCommand.of("debug", "Debug of a guild")
                                         .handler(new Debug(guildRepository))
                                         .argument(Argument.text("guild_id", "Id of guild").asRequired())
                                         .argument(Argument.text("channel_id", "Id of channel")))
                   .group(Group.of("log", "log access")
                               .subCommand(SubCommand.of("analyzer", "Analyzer log")
                                                     .handler(new Analyzer(guildRepository))
                                                     .argument(Argument.text("guildid", "Guild id").asRequired())
                                                     .argument(Argument.text("messageid", "Id of message").asRequired())))
                   .subCommand(SubCommand.of("shared_guilds", "Shared guilds with a user")
                                         .handler(new SharedGuilds(configuration))
                                         .argument(Argument.text("user_id", "user id"))
                                         .argument(Argument.user("user", "user")))
                   .subCommand(SubCommand.of("redeploy", "Redeploy guild commands")
                                         .handler(new Redeploy())
                                         .argument(Argument.text("guild_id", "Guild id").asRequired()))
                   .subCommand(SubCommand.of("search", "Search for guilds")
                                         .handler(new Search())
                                         .argument(Argument.text("term", "Search term").asRequired()))
                   .subCommand(SubCommand.of("invalidate_cache", "Invalidates cached data for the guild")
                                         .handler(new InvalidateCache(guildRepository))
                                         .argument(Argument.text("guild", "guild id").asRequired()))
                   .subCommand(SubCommand.of("leave", "Leave a guild")
                                         .handler(new Leave())
                                         .argument(Argument.text("guild_id", "Guild id").asRequired()))
                   .group(group("entitlement", "Manage entitlements")
                           .subCommand(sub("create", "Create entitlements")
                                   .handler(new Create(configuration))
                                   .argument(text("sku", "skuid").asRequired().withAutoComplete())
                                   .argument(text("ownerid", "guild id")))
                           .subCommand(sub("list", "List entitlements of a guild")
                                   .handler(new Show())
                                   .argument(text("guild_id", "Guild id")))
                           .subCommand(sub("delete", "Delete Entitlement")
                                   .handler(new Delete())
                                   .argument(text("entitlementid", "Entitlement id").asRequired())))
                .subCommand(sub("sample", "Generate sample data")
                        .handler(new Sample(guildRepository)))
                .build();
    }

    @Override
    public Slash slash() {
        return slash;
    }
}
