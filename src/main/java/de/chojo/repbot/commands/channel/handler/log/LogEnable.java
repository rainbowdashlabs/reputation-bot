/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel.handler.log;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.Premium;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class LogEnable implements SlashHandler {
    private final GuildRepository guildRepository;
    private final Configuration configuration;

    public LogEnable(GuildRepository guildRepository, Configuration configuration) {
        this.guildRepository = guildRepository;
        this.configuration = configuration;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        GuildChannelUnion channel = event.getOption("channel", OptionMapping::getAsChannel);
        if (channel.getType() != ChannelType.TEXT) {
            event.reply(context.localize("error.onlyTextChannel")).setEphemeral(true).queue();
            return;
        }

        if (Premium.checkAndReplyPremium(context, configuration.skus().features().logChannel().logChannel())) {
            return;
        }

        guildRepository.guild(event.getGuild()).settings().logChannel().channel(channel.asTextChannel());
        guildRepository.guild(event.getGuild()).settings().logChannel().active(true);

        event.reply(context.localize("command.channel.log.channel.message.enabled", Replacement.createMention(channel.asTextChannel()))).setEphemeral(true).queue();
    }
}
