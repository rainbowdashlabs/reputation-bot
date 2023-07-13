/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.scan.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.commands.scan.util.Scanner;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Cancel implements SlashHandler {
    private final Scanner scanner;

    public Cancel(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        if (!scanner.isActive(event.getGuild())) {
            event.reply(context.localize("command.scan.cancel.message.notask")).setEphemeral(true).queue();
            return;
        }
        event.reply(context.localize("command.scan.cancel.message.canceling")).queue();
        scanner.cancelScan(event.getGuild());
    }
}
