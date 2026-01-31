/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel.handler.autopost;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.settings.sub.autopost.Autopost;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class AutopostInfo implements SlashHandler {
    private final GuildRepository guildRepository;

    public AutopostInfo(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Autopost autopost = guildRepository.guild(event.getGuild()).settings().autopost();

        if (!autopost.active()) {
            event.reply(context.localize("command.channel.autopost.info.message.inactive"))
                 .setEphemeral(true)
                 .complete();
            return;
        }

        MessageEmbed embed = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.channel.autopost.info.embed.title")
                .addField("words.channel", MentionUtil.channel(autopost.channelId()), true)
                .addField("command.channel.autopost.info.embed.refreshinterval", autopost.refreshInterval().toString(), true)
                .addField("command.channel.autopost.info.embed.refreshtype", autopost.refreshType().toString(), true)
                .build();
        event.replyEmbeds(embed)
             .setEphemeral(true)
             .complete();
    }
}
