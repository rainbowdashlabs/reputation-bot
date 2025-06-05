/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.ranking.handler.user;

import de.chojo.repbot.commands.ranking.handler.BaseTop;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.pagination.Ranking;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Optional;

public class UserGiven extends BaseTop {
    public UserGiven(GuildRepository guildRepository) {
        super(guildRepository);
    }

    @Override
    protected Ranking buildRanking(SlashCommandInteractionEvent event, RepGuild guild, ReputationMode reputationMode, int pageSize) {
        Member user = Optional.ofNullable(event.getOption("user")).map(OptionMapping::getAsMember).orElse(event.getMember());
        return guild.reputation().ranking().user().received().byMode(reputationMode, pageSize, user);
    }
}
