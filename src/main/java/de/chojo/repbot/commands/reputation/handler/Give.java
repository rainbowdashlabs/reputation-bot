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
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

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

        if (!guild.settings().reputation().isCommandActive()) {
            event.reply("not active").setEphemeral(true).queue();
            return;
        }

        var result = reputationService.submitReputation(event.getGuild(), event.getMember(), event.getOption("user", OptionMapping::getAsMember), ReputationContext.fromInteraction(event), null, ThankType.COMMAND);
        if (result.type() == SubmitResultType.SUCCESS) {
            event.reply("success").setEphemeral(guild.settings().messages().isReactionConfirmation()).queue();
        } else {
            event.reply(context.guildLocale(result.type().localeKey(), result.replacements().toArray(Replacement[]::new))).setEphemeral(true).queue();
        }
    }
}
