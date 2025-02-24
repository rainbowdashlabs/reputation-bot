/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.scan.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.commands.scan.util.ScanProcess;
import de.chojo.repbot.commands.scan.util.Scanner;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Handler for the "start scan" slash command.
 * This command initiates a scan process in a specified text channel.
 */
public class Start implements SlashHandler {
    private final Scanner scanner;

    /**
     * Constructs a new Start handler.
     *
     * @param scanner the scanner utility
     */
    public Start(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Handles the slash command interaction event.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        if (!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_HISTORY)) {
            event.reply(context.localize("command.scan.start.message.history")).setEphemeral(true).queue();
            return;
        }

        if (scanner.isActive(event.getGuild())) {
            event.reply(":stop_sign: " + context.localize("command.scan.start.message.running")).setEphemeral(true)
                 .queue();
            return;
        }

        if (scanner.limitReached()) {
            event.reply(":stop_sign: " + context.localize("command.scan.start.message.queueFull")).setEphemeral(true)
                 .queue();
            return;
        }

        if (event.getOptions().isEmpty()) {
            scanner.scanChannel(event, context, event.getChannel().asTextChannel(), ScanProcess.MAX_MESSAGES);
            return;
        }

        var messages = ScanProcess.MAX_MESSAGES;
        var channel = event.getChannel().asTextChannel();
        if (event.getOption("numbermessages") != null) {
            messages = (int) event.getOption("numbermessages").getAsLong();
        }
        if (event.getOption("channel") != null) {
            var guildChannel = event.getOption("channel").getAsChannel();
            if (guildChannel.getType() != ChannelType.TEXT) {
                event.reply(context.localize("error.invalidChannel")).queue();
                return;
            }
            channel = (TextChannel) guildChannel;
        }

        scanner.scanChannel(event, context, channel, Math.max(messages, 0));
    }
}
