/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.reputation;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public record ReputationContext(GuildMessageChannel guildChannel, ISnowflake snowflake) {
    public static ReputationContext fromMessage(Message message) {
        return new ReputationContext(message.getGuildChannel(), message);
    }

    public static ReputationContext fromInteraction(SlashCommandInteractionEvent event) {
        return new ReputationContext(event.getGuildChannel(), event);
    }

    public long getIdLong() {
        return snowflake.getIdLong();
    }

    public GuildMessageChannel getChannel() {
        return guildChannel;
    }

    public Guild getGuild() {
        return guildChannel.getGuild();
    }

    public boolean isMessage() {
        return snowflake instanceof Message;
    }

    public boolean isInteraction() {
        return snowflake instanceof GenericInteractionCreateEvent;
    }

    public Message asMessage() {
        return (Message) snowflake;
    }

    public Message getLastMessage() {
        if (isMessage()) return asMessage();
        return getChannel().getHistory().retrievePast(1).complete().get(0);
    }
}
