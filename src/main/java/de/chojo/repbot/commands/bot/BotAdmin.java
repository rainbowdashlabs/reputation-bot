package de.chojo.repbot.commands.bot;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.bot.handler.Debug;
import de.chojo.repbot.commands.bot.handler.Leave;
import de.chojo.repbot.commands.bot.handler.Redeploy;
import de.chojo.repbot.commands.bot.handler.Search;
import de.chojo.repbot.commands.bot.handler.SharedGuilds;
import de.chojo.repbot.dao.provider.Guilds;

public class BotAdmin extends SlashCommand {
    public BotAdmin(Guilds guilds) {
        super(Slash.of("bot", "Bot admin commands.")
                .unlocalized()
                .guildOnly()
                .adminCommand()
                .privateCommand()
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
                .subCommand(SubCommand.of("leave", "Leave a guild")
                        .handler(new Leave())
                        .argument(Argument.text("guild_id", "Guild id").asRequired())));
    }
}
