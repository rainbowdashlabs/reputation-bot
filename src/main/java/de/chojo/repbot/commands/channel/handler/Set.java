/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel.handler;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.settings.sub.thanking.Channels;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Set extends BaseChannelModifier {
    public Set(GuildRepository guildRepository) {
        super(guildRepository);
    }

    @Override
    public void textChannel(SlashCommandInteractionEvent event, EventContext context, Channels channels, StandardGuildChannel channel) {
        channels.clearChannel();
        channels.add(channel);
        event.getHook().editOriginal(
                context.localize("command.channel.set.message.set",
                        Replacement.create("CHANNEL", channel.getAsMention()))).complete();
    }

    @Override
    public void category(SlashCommandInteractionEvent event, EventContext context, Channels channels, Category category) {
        channels.clearCategories();
        channels.add(category);
        event.getHook().editOriginal(
                context.localize("command.channel.set.message.set",
                        Replacement.create("CHANNEL", category.getAsMention()))).complete();
    }
}
