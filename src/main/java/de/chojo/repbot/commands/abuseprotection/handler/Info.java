/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.abuseprotection.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.Color;
import java.util.List;

import static de.chojo.repbot.util.Text.getSetting;

public class Info implements SlashHandler {
    private final Guilds guilds;

    public Info(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        event.replyEmbeds(getSettings(context, guilds.guild(event.getGuild()))).queue();
    }

    private MessageEmbed getSettings(EventContext context, RepGuild guild) {
        var abuseProt = guild.settings().abuseProtection();
        var setting = List.of(
                getSetting("command.abuseprotection.info.message.maxMessageAge", abuseProt.maxMessageAge()),
                getSetting("command.abuseprotection.info.message.minMessages", abuseProt.minMessages()),
                getSetting("command.abuseprotection.info.message.cooldown", abuseProt.cooldown()),
                getSetting("command.abuseprotection.info.message.donorContext", abuseProt.isDonorContext()),
                getSetting("command.abuseprotection.info.message.receiverContext", abuseProt.isReceiverContext()),
                getSetting("command.abuseprotection.info.message.maxMessageRep", abuseProt.maxMessageReputation())
        );

        var settings = String.join("\n", setting);

        return new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.abuseprotection.info.message.title")
                .appendDescription(settings)
                .setColor(Color.GREEN)
                .build();
    }
}
