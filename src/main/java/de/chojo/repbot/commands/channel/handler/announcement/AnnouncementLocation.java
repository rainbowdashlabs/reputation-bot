/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel.handler.announcement;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class AnnouncementLocation implements SlashHandler {
    private final GuildRepository guildRepository;

    public AnnouncementLocation(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var announcements = guildRepository.guild(event.getGuild()).settings().announcements();
        var channel = event.getOption("channel");
        if (channel.getChannelType() != ChannelType.TEXT) {
            event.reply(context.localize("error.onlyTextChannel")).setEphemeral(true).complete();
            return;
        }

        announcements.channel(channel.getAsChannel().asTextChannel());
        event.reply(context.localize("command.channel.announcement.channel.message.set",
                Replacement.createMention(channel.getAsChannel().asTextChannel()))).complete();
    }
}
