/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel.handler.announcement;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class AnnouncementInfo implements SlashHandler {
    private final GuildRepository guildRepository;

    public AnnouncementInfo(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var announcements = guildRepository.guild(event.getGuild()).settings().announcements();
        if (!announcements.isActive()) {
            event.reply(WebPromo.promoString(context) + context.localize("command.channel.announcement.state.message.inactive"))
                 .setEphemeral(true)
                 .complete();
            return;
        }
        if (announcements.isSameChannel()) {
            event.reply(WebPromo.promoString(context) + context.localize("command.channel.announcement.location.message.samechannel"))
                 .setEphemeral(true)
                 .complete();
            return;
        }
        event.reply(WebPromo.promoString(context) + context.localize("command.channel.announcement.channel.message.set",
                     Replacement.create("CHANNEL", MentionUtil.channel(announcements.channelId()))))
             .setEphemeral(true)
             .complete();
    }
}
