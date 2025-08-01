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

public class Status implements SlashHandler {
    private final Statistic statistic;

    public Status(Statistic statistic) {
        this.statistic = statistic;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var builder = new EmbedBuilder();
        var systemStatistic = statistic.getSystemStatistic();
        systemStatistic.appendTo(builder);
        event.replyEmbeds(builder.build()).complete();
    }
}
