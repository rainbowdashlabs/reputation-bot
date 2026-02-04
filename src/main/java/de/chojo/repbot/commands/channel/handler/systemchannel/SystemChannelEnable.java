/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel.handler.systemchannel;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static net.dv8tion.jda.api.entities.channel.ChannelType.TEXT;

public class SystemChannelEnable implements SlashHandler {
    private final GuildRepository guildRepository;

    public SystemChannelEnable(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        GuildChannelUnion channel = event.getOption("channel").getAsChannel();

        if (channel.getType() != TEXT) {
            event.reply(context.localize("error.onlyTextChannel"))
                    .setEphemeral(true)
                    .complete();
            return;
        }

        guildRepository.guild(event.getGuild()).settings().general().systemChannel(channel.getIdLong());
        event.reply(WebPromo.promoString(context)
                        + context.localize(
                                "command.channel.systemchannel.enable.message.enabled",
                                Replacement.create("CHANNEL", channel.getAsMention())))
                .setEphemeral(true)
                .complete();
    }
}
