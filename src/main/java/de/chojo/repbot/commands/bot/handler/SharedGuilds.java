/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.util.Guilds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class SharedGuilds implements SlashHandler {
    private final Configuration configuration;

    public SharedGuilds(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var userOpt = event.getOption("user");
        var userIdOpt = event.getOption("user_id");
        var deepSearch = event.getOption("deep", () -> false, OptionMapping::getAsBoolean);
        if (userOpt == null && userIdOpt == null) {
            event.reply("Provide a user").setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue();

        event.getHook().editOriginal("Searching for user in cache").queue();

        User user;

        if (userOpt != null) {
            user = userOpt.getAsUser();
        } else {
            try {
                user = event.getJDA().getShardManager().retrieveUserById(event.getIdLong()).complete();
            } catch (RuntimeException e) {
                event.getHook().editOriginal("Could not find this user.").queue();
                return;
            }
        }

        event.getHook().editOriginal("Searching for shared guilds in cache").queue();

        var mutualGuilds = new ArrayList<>(event.getJDA().getShardManager().getMutualGuilds(user));
        mutualGuilds.removeIf(g -> configuration.baseSettings().botGuild() == g.getIdLong());

        if (mutualGuilds.isEmpty() || deepSearch) {
            event.getHook().editOriginal("Performing deep search for user").queue();
            for (var shard : event.getJDA().getShardManager().getShards()) {
                for (var guild : shard.getGuilds()) {
                    var member = guild.retrieveMemberById(user.getIdLong()).complete();
                    if (member != null) mutualGuilds.add(guild);
                }
            }
        }

        var guilds = mutualGuilds.stream()
                .map(g -> "%s Owner: %s".formatted(Guilds.prettyName(g), g.getOwnerIdLong() == user.getIdLong()))
                .collect(Collectors.joining("\n"));

        event.getHook().editOriginal("Search done").queue();

        event.getHook().editOriginalEmbeds(new EmbedBuilder().setTitle("Shared Guilds").setDescription(guilds).build())
                .queue();
    }
}
