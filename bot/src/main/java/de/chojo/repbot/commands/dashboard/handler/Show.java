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
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.Colors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.stream.Collectors;

public class Show implements SlashHandler {
    private final GuildRepository guildRepository;

    public Show(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        event.replyEmbeds(getDashboard(event.getGuild(), context)).complete();
    }

    private MessageEmbed getDashboard(Guild guild, EventContext context) {
        var reputation = guildRepository.guild(guild).reputation();
        var stats = reputation.stats();
        var top = reputation.ranking().received().total(5).page(0).stream()
                .map(r -> r.fancyString(5))
                .collect(Collectors.joining("\n"));

        return new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.dashboard.message.title", Replacement.create("GUILD", guild.getName()))
                .setThumbnail(
                        guild.getIconUrl() == null
                                ? guild.getSelfMember().getUser().getAvatarUrl()
                                : guild.getIconUrl())
                .setColor(Colors.Pastel.BLUE)
                .addField("command.dashboard.message.topUser", top, false)
                .addField("command.dashboard.message.totalReputation", String.valueOf(stats.totalReputation()), true)
                .addField("command.dashboard.message.weekReputation", String.valueOf(stats.weekReputation()), true)
                .addField("command.dashboard.message.todayReputation", String.valueOf(stats.todayReputation()), true)
                .addField("command.dashboard.message.topChannel", MentionUtil.channel(stats.topChannelId()), true)
                .build();
    }
}
