/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ListType implements SlashHandler {
    private final GuildRepository guildRepository;

    public ListType(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var channels = guildRepository.guild(event.getGuild()).settings().thanking().channels();
        if (event.getOptions().isEmpty()) {
            event.reply(context.localize(
                         channels.isWhitelist() ? "command.channel.listType.message.whitelist" : "command.channel.listType.message.blacklist"))
                 .queue();
            return;
        }
        var whitelist = "whitelist".equalsIgnoreCase(event.getOption("type").getAsString());

        event.reply(context.localize(
                     channels.listType(whitelist) ? "command.channel.listType.message.whitelist" : "command.channel.listType.message.blacklist"))
             .queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        if ("type".equals(event.getFocusedOption().getName())) {
            event.replyChoices(Completion.complete(event.getFocusedOption().getValue(), "whitelist", "blacklist"))
                 .queue();
        }
    }
}
