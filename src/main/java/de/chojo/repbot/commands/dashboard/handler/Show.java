/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.dashboard.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.Colors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.stream.Collectors;

/**
 * Handler for the "show dashboard" slash command.
 * This command displays a dashboard with various statistics and rankings for the guild.
 */
public class Show implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs a new Show handler.
     *
     * @param guilds the guilds provider
     */
    public Show(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        event.replyEmbeds(getDashboard(event.getGuild(), context)).queue();
    }

    /**
     * Generates the dashboard embed message for the guild.
     *
     * @param guild the guild for which the dashboard is generated
     * @param context the event context
     * @return the dashboard embed message
     */
    private MessageEmbed getDashboard(Guild guild, EventContext context) {
        var reputation = guilds.guild(guild).reputation();
        var stats = reputation.stats();
        var top = reputation.ranking().total(5).page(0).stream()
                            .map(r -> r.fancyString(5))
                            .collect(Collectors.joining("\n"));

        return new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.dashboard.message.title",
                        Replacement.create("GUILD", guild.getName()))
                .setThumbnail(guild.getIconUrl() == null ? guild.getSelfMember().getUser()
                                                                .getAvatarUrl() : guild.getIconUrl())
                .setColor(Colors.Pastel.BLUE)
                .addField("command.dashboard.message.topUser", top, false)
                .addField("command.dashboard.message.totalReputation", String.valueOf(stats.totalReputation()), true)
                .addField("command.dashboard.message.weekReputation", String.valueOf(stats.weekReputation()), true)
                .addField("command.dashboard.message.todayReputation", String.valueOf(stats.todayReputation()), true)
                .addField("command.dashboard.message.topChannel", MentionUtil.channel(stats.topChannelId()), true)
                .build();
    }
}
