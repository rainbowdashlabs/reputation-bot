/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel.handler.autopost;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.jdautil.util.Premium;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.settings.sub.autopost.Autopost;
import de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshInterval;
import de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshType;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.AutopostService;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

public class AutopostEnable implements SlashHandler {
    private final GuildRepository guildRepository;
    private final Configuration configuration;
    private final AutopostService autopostService;

    public AutopostEnable(GuildRepository guildRepository, Configuration configuration, AutopostService autopostService) {
        this.guildRepository = guildRepository;
        this.configuration = configuration;
        this.autopostService = autopostService;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        if (Premium.checkAndReplyPremium(context, configuration.skus().features().autopost().autopostChannel())) {
            return;
        }

        event.deferReply(true).complete();

        GuildChannelUnion channel = event.getOption("channel", OptionMapping::getAsChannel);
        if (channel.getType() != ChannelType.TEXT) {
            event.reply(context.localize("error.onlyTextChannel")).setEphemeral(true).queue();
            return;
        }
        Autopost autopost = guildRepository.guild(event.getGuild()).settings().autopost();

        if(autopost.active()){
            autopostService.delete(event.getGuild());
        }

        autopost.active(true);
        TextChannel textChannel = channel.asTextChannel();
        autopost.channel(textChannel);

        if (event.getOption("refreshinterval") != null) {
            String refreshInterval = event.getOption("refreshinterval").getAsString();
            autopost.refreshInterval(RefreshInterval.valueOf(refreshInterval));
        }

        if (event.getOption("refreshtype") != null) {
            String refreshType = event.getOption("refreshtype").getAsString();
            autopost.refreshType(RefreshType.valueOf(refreshType));
        }

        autopostService.update(event.getGuild());

        event.getHook().editOriginal(context.localize("command.channel.autopost.enable.message.enabled",
                Replacement.create("CHANNEL", MentionUtil.channel(autopost.channelId())))).queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        if (event.getFocusedOption().getName().equals("refreshinterval")) {
            List<Command.Choice> complete = Completion.complete(event.getFocusedOption().getValue(), RefreshInterval.class, false, false);
            event.replyChoices(complete).queue();
        }
        if (event.getFocusedOption().getName().equals("refreshtype")) {
            List<Command.Choice> complete = Completion.complete(event.getFocusedOption().getValue(), RefreshType.class, false, false);
            event.replyChoices(complete).queue();
        }
    }
}
