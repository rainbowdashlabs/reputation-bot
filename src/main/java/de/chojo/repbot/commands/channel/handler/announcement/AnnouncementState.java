/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel.handler.announcement;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class AnnouncementState implements SlashHandler {
    private final GuildRepository guildRepository;

    public AnnouncementState(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var announcements = guildRepository.guild(event.getGuild()).settings().announcements();
        if (announcements.active(event.getOption("active", OptionMapping::getAsBoolean))) {
            event.reply(context.localize("command.channel.announcement.state.message.active")).queue();
        } else {
            event.reply(context.localize("command.channel.announcement.state.message.inactive")).queue();
        }
    }
}
