/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.locale.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;

public class Set implements SlashHandler {
    private final GuildRepository guildRepository;

    public Set(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        DiscordLocale locale = null;
        for (var value : DiscordLocale.values()) {
            if (value.getNativeName().equals(event.getOption("language").getAsString())) {
                locale = value;
            }
        }

        if (locale == null || !context.guildLocalizer().localizer().languages().contains(locale)) {
            event.reply(context.localize("command.locale.set.message.invalidlocale"))
                    .setEphemeral(true)
                    .complete();
            return;
        }

        if (guildRepository.guild(event.getGuild()).settings().general().language(locale)) {
            event.reply(WebPromo.promoString(context)
                            + context.localize(
                                    "command.locale.set.message.set",
                                    Replacement.create("LOCALE", locale.getNativeName(), Format.CODE)))
                    .setEphemeral(true)
                    .complete();
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        var option = event.getFocusedOption();
        if ("language".equalsIgnoreCase(option.getName())) {
            event.replyChoices(Completion.complete(
                            option.getValue(),
                            context.guildLocalizer().localizer().languages(),
                            DiscordLocale::getNativeName))
                    .complete();
        }
    }
}
