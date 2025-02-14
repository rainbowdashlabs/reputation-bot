/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler.system;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.statistic.Statistic;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Handler for the status slash command.
 */
public class Status implements SlashHandler {
    private final Statistic statistic;

    /**
     * Constructs a new Status handler.
     *
     * @param statistic the statistic object used to retrieve system statistics
     */
    public Status(Statistic statistic) {
        this.statistic = statistic;
    }

    /**
     * Handles the slash command interaction event.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var builder = new EmbedBuilder();
        var systemStatistic = statistic.getSystemStatistic();
        systemStatistic.appendTo(builder);
        event.replyEmbeds(builder.build()).queue();
    }
}
