/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.thankwords.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Add implements SlashHandler {
    private final GuildRepository guildRepository;
    private final List<String> invalid = List.of("(", ")", "{", "}", "*", "\\s", " ", ".", "#", "<", ">");
    public Add(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var pattern = event.getOption("pattern").getAsString();

        // Escape channels that are smh used as thankwords.
        pattern = pattern.replaceAll("<#(.+?)>", "$1");

        for (String character : invalid) {
            if (pattern.contains(character)) {
                event.reply(context.localize("error.invalidcharacter", Replacement.create("INVALID", character, Format.CODE)))
                     .setEphemeral(true)
                     .queue();
                return;
            }
        }

        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            event.reply(context.localize("error.invalidRegex"))
                 .setEphemeral(true)
                 .queue();
            return;
        }

        if (guildRepository.guild(event.getGuild()).settings().thanking().thankwords().add(pattern)) {
            event.reply(context.localize("command.thankwords.add.message.added",
                    Replacement.create("REGEX", pattern, Format.CODE))).queue();
        }
    }
}
