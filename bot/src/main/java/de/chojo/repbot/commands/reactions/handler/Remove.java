/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.reactions.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Choice;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.regex.Pattern;

public class Remove implements SlashHandler {
    private static final Pattern EMOTE_PATTERN = Pattern.compile("<a?:.*?:(?<id>\\d*?)>");

    private final GuildRepository guildRepository;

    public Remove(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var reactions =
                guildRepository.guild(event.getGuild()).settings().thanking().reactions();
        var emote = event.getOption("emote").getAsString();
        var matcher = EMOTE_PATTERN.matcher(emote);
        if (matcher.find()) {
            if (reactions.remove(matcher.group("id"))) {
                event.reply(WebPromo.promoString(context) + "\n"
                                + context.localize("command.reactions.remove.message.removed"))
                        .setEphemeral(true)
                        .complete();
                return;
            }
            event.reply(WebPromo.promoString(context) + "\n"
                            + context.localize("command.reactions.remove.message.notfound"))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (reactions.remove(emote)) {
            event.reply(WebPromo.promoString(context) + "\n"
                            + context.localize("command.reactions.remove.message.removed"))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        event.reply(WebPromo.promoString(context) + "\n"
                        + context.localize("command.reactions.remove.message.notfound"))
                .setEphemeral(true)
                .queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        if ("emote".equals(event.getFocusedOption().getName())) {
            var reactions =
                    guildRepository.guild(event.getGuild()).settings().thanking().reactions().reactions().stream()
                            .limit(25)
                            .map(Choice::toChoice)
                            .toList();
            event.replyChoices(reactions).queue();
        }
    }
}
