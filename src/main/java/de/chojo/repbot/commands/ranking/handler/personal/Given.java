/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.ranking.handler.personal;

import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.commands.ranking.handler.BaseTop;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Given extends BaseTop {
    private final GuildRepository guildRepository;

    public Given(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild = guildRepository.guild(event.getGuild());
        var reputationMode = guild.settings().general().reputationMode();
        if (event.getOption("mode") != null) {
            var mode = event.getOption("mode").getAsString();
            reputationMode = switch (mode) {
                case "total" -> ReputationMode.TOTAL;
                case "7 days" -> ReputationMode.ROLLING_WEEK;
                case "30 days" -> ReputationMode.ROLLING_MONTH;
                case "week" -> ReputationMode.WEEK;
                case "month" -> ReputationMode.MONTH;
                default -> reputationMode;
            };
        }

        var ranking = guild.reputation().ranking().received().byMode(reputationMode, TOP_PAGE_SIZE);
        registerPage(ranking, event, context);
    }

}
