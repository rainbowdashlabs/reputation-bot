/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.vote.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.VoteRepository;
import de.chojo.repbot.util.Colors;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Show implements SlashHandler {
    private final Configuration configuration;
    private final VoteRepository voteRepository;

    public Show(Configuration configuration, VoteRepository voteRepository) {
        this.configuration = configuration;
        this.voteRepository = voteRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guildId = event.getGuild().getId();
        var baseUrl = configuration.api().url();
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        var tokenShopUrl = baseUrl + "guild/token-shop?guild=" + guildId;
        var voteOverviewUrl = baseUrl + "user/vote?guild=" + guildId;

        var lb = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setDescription("command.vote.message.text", Replacement.create("URL", tokenShopUrl))
                .setColor(Colors.Pastel.BLUE);

        // Per-botlist last vote and streak
        long userId = event.getUser().getIdLong();
        for (var botlist : configuration.botlist().botlists()) {
            if (botlist.voteUrl().isBlank()) continue;
            var streak = voteRepository.getLastVote(userId, botlist.name());
            String lastVoteText;
            if (streak.lastVote() == null || streak.lastVote().equals(Instant.EPOCH)) {
                lastVoteText = context.localize("command.vote.field.never");
            } else {
                var epoch = streak.lastVote().getEpochSecond();
                lastVoteText = "<t:" + epoch + ":R>"; // Discord relative timestamp
            }
            String value = "$command.vote.field.lastVote$: %s\n$command.vote.field.streak$: %d"
                    .formatted(lastVoteText, streak.streak());
            lb.addField(" " + botlist.name(), value, true);
        }

        MessageEmbed embed = lb.build();

        List<Button> botlistButtons = new ArrayList<>();
        for (var botlist : configuration.botlist().botlists()) {
            if (!botlist.voteUrl().isBlank()) {
                botlistButtons.add(Button.link(botlist.voteUrl(), botlist.name()));
            }
        }

        Button overviewButton =
                Button.link(voteOverviewUrl, context.guildLocalizer().localize("command.vote.button.overview"));

        if (botlistButtons.isEmpty()) {
            event.replyEmbeds(embed).setComponents(ActionRow.of(overviewButton)).complete();
        } else {
            event.replyEmbeds(embed)
                    .setEphemeral(true)
                    .setComponents(ActionRow.of(botlistButtons), ActionRow.of(overviewButton))
                    .complete();
        }
    }
}
