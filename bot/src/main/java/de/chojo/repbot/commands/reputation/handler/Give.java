/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.reputation.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.reputation.ReputationContext;
import de.chojo.repbot.service.reputation.ReputationService;
import de.chojo.repbot.service.reputation.SubmitResultType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Collections;

public class Give implements SlashHandler {
    private final GuildRepository guilds;
    private final ReputationService reputationService;

    public Give(GuildRepository guilds, ReputationService reputationService) {
        this.guilds = guilds;
        this.reputationService = reputationService;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        RepGuild guild = guilds.guild(event.getGuild());

        // The command should not be available in this case, but better check.
        if (!guild.settings().reputation().isCommandActive()) {
            event.reply("Command disabled").setEphemeral(true).queue();
            return;
        }

        Member donor = event.getMember();
        Member receiver = event.getOption("user", OptionMapping::getAsMember);
        var result = reputationService.submitReputation(
                event.getGuild(), donor, receiver, ReputationContext.fromInteraction(event), null, ThankType.COMMAND);
        if (result.type() == SubmitResultType.SUCCESS) {
            event.reply(context.guildLocale(
                            "command.reputation.give.message.success",
                            Replacement.createMention("DONOR", donor),
                            Replacement.createMention("RECEIVER", receiver)))
                    .mentionUsers(Collections.emptyList())
                    .setEphemeral(guild.settings().messages().isCommandReputationEphemeral())
                    .queue();
        } else {
            event.reply(context.guildLocale(
                            result.type().localeKey(), result.replacements().toArray(Replacement[]::new)))
                    .setEphemeral(true)
                    .queue();
        }
    }
}
