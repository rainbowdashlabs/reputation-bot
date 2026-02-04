/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.thankwords.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.serialization.ThankwordsContainer;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.stream.Collectors;

public class LoadDefault implements SlashHandler {
    private final GuildRepository guildRepository;
    private final ThankwordsContainer thankwordsContainer;

    public LoadDefault(GuildRepository guildRepository, ThankwordsContainer thankwordsContainer) {
        this.guildRepository = guildRepository;
        this.thankwordsContainer = thankwordsContainer;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var languageOption = event.getOption("language");
        if (languageOption == null) {
            event.reply(WebPromo.promoString(context) + "\n" + context.localize("command.thankwords.loaddefault.message.available")
                         + " " + String.join(", ", thankwordsContainer.getAvailableLanguages()))
                 .setEphemeral(true)
                 .complete();
            return;
        }
        var language = languageOption.getAsString();
        var words = thankwordsContainer.get(language.toLowerCase(Locale.ROOT));
        if (words == null) {
            event.reply(WebPromo.promoString(context) + "\n" + context.localize("command.locale.set.message.invalidlocale"))
                 .setEphemeral(true)
                 .complete();
            return;
        }
        for (var word : words) {
            guildRepository.guild(event.getGuild()).settings().thanking().thankwords().add(word);
        }

        var wordsJoined = words.stream().map(w -> StringUtils.wrap(w, "`")).collect(Collectors.joining(", "));

        event.reply(WebPromo.promoString(context) + "\n" + context.localize("command.thankwords.loaddefault.message.added") + wordsJoined)
             .setEphemeral(true)
             .complete();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        var option = event.getFocusedOption();
        event.replyChoices(Completion.complete(option.getValue(), thankwordsContainer.getAvailableLanguages())).queue();
    }
}
