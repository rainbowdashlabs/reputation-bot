/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.ranking.handler.guild;

import de.chojo.repbot.commands.ranking.handler.BaseTop;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.pagination.Ranking;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class GuildReceived extends BaseTop {

    public GuildReceived(GuildRepository guildRepository, Configuration configuration, boolean premium) {
        super(guildRepository, configuration, premium);
    }

    @Override
    protected Ranking buildRanking(SlashCommandInteractionEvent event, RepGuild guild, ReputationMode reputationMode, int pageSize) {
        return guild.reputation().ranking().received().byMode(reputationMode, pageSize);
    }
}
