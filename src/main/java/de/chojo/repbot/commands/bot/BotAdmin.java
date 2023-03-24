package de.chojo.repbot.commands.bot;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Group;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.bot.handler.Debug;
import de.chojo.repbot.commands.bot.handler.InvalidateCache;
import de.chojo.repbot.commands.bot.handler.Leave;
import de.chojo.repbot.commands.bot.handler.Redeploy;
import de.chojo.repbot.commands.bot.handler.Search;
import de.chojo.repbot.commands.bot.handler.SharedGuilds;
import de.chojo.repbot.commands.bot.handler.system.Metrics;
import de.chojo.repbot.commands.bot.handler.system.Reload;
import de.chojo.repbot.commands.bot.handler.system.Restart;
import de.chojo.repbot.commands.bot.handler.system.Shudown;
import de.chojo.repbot.commands.bot.handler.system.Status;
import de.chojo.repbot.commands.bot.handler.system.Upgrade;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.statistic.Statistic;

public class BotAdmin extends SlashCommand {
    public BotAdmin(Guilds guilds, Configuration configuration, Statistic statistics) {
        super(Slash.of("bot", "Bot admin commands.")
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
                        .handler(new Debug(guilds))
                        .argument(Argument.text("guild_id", "Id of guild").asRequired()))
                .subCommand(SubCommand.of("shared_guilds", "Shared guilds with a user")
                        .handler(new SharedGuilds())
                        .argument(Argument.text("user_id", "user id"))
                        .argument(Argument.user("user", "user")))
                .subCommand(SubCommand.of("redeploy", "Redeploy guild commands")
                        .handler(new Redeploy())
                        .argument(Argument.text("guild_id", "Guild id").asRequired()))
                .subCommand(SubCommand.of("search", "Search for guilds")
                        .handler(new Search())
                        .argument(Argument.text("term", "Search term").asRequired()))
                .subCommand(SubCommand.of("invalidate_cache", "Invalidates cached data for the guild")
                        .handler(new InvalidateCache(guilds))
                        .argument(Argument.text("guild", "guild id").asRequired()))
                .subCommand(SubCommand.of("leave", "Leave a guild")
                        .handler(new Leave())
                        .argument(Argument.text("guild_id", "Guild id").asRequired())));
    }
}
