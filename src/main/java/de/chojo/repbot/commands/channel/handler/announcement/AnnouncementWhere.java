/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel.handler.announcement;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class AnnouncementWhere implements SlashHandler {
    private final GuildRepository guildRepository;

    public AnnouncementWhere(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var announcements = guildRepository.guild(event.getGuild()).settings().announcements();
        if (announcements.sameChannel("same channel".equalsIgnoreCase(event.getOption("where", OptionMapping::getAsString)))) {
            event.reply(context.localize("command.channel.announcement.location.message.samechannel")).queue();
        } else {
            event.reply(context.localize("command.channel.announcement.location.message.otherchannel")).queue();
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        if ("where".equals(event.getFocusedOption().getName())) {
            event.replyChoices(Completion.complete("", "same channel", "custom channel")).queue();
        }
    }
}
