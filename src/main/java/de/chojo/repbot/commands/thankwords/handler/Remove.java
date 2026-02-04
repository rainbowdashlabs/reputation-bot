/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.thankwords.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Remove implements SlashHandler {
    private final GuildRepository guildRepository;

    public Remove(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var pattern = event.getOption("pattern").getAsString();
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            event.reply(WebPromo.promoString(context) + "\n" + context.localize("error.invalidRegex"))
                 .setEphemeral(true)
                 .complete();
            return;
        }
        if (guildRepository.guild(event.getGuild()).settings().thanking().thankwords().remove(pattern)) {
            event.reply(WebPromo.promoString(context) + "\n" + context.localize("command.thankwords.remove.message.removed",
                         Replacement.create("PATTERN", pattern, Format.CODE)))
                 .setEphemeral(true)
                 .complete();
            return;
        }
        event.reply(WebPromo.promoString(context) + "\n" + context.localize("command.thankwords.remove.message.patternnotfound"))
             .setEphemeral(true)
             .complete();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        var option = event.getFocusedOption();
        var thankwords = guildRepository.guild(event.getGuild()).settings().thanking().thankwords().words();
        event.replyChoices(Completion.complete(option.getValue(), thankwords)).queue();
    }
}
