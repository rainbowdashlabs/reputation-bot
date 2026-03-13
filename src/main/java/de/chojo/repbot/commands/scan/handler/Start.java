/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.scan.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.service.ScanService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Start implements SlashHandler {
    private final ScanService scanService;
    private final Configuration configuration;

    public Start(ScanService scanService, Configuration configuration) {
        this.scanService = scanService;
        this.configuration = configuration;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        String url = configuration.api().pathUrl(event.getGuild().getIdLong(), "/settings/edit/scan");
        event.reply(context.localize("command.start.message"))
                .setEphemeral(true)
                .complete();
        scanService.scan(event.getGuild());
    }
}
