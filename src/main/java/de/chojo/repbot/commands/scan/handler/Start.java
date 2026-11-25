/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.scan.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.ScanService;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Start implements SlashHandler {
    private final ScanService scanService;
    private final Configuration configuration;
    private final GuildRepository guildRepository;

    public Start(ScanService scanService, Configuration configuration, GuildRepository guildRepository) {
        this.scanService = scanService;
        this.configuration = configuration;
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        if (guildRepository.guild(event.getGuild()).subscriptions().isBlocked()) {
            event.reply(context.localize(
                            "submitresult.supporterblocked",
                            Replacement.create(
                                    "URL",
                                    configuration
                                            .api()
                                            .debugUrl(event.getGuild().getIdLong()))))
                    .setEphemeral(true)
                    .complete();
            return;
        }
        String url = configuration.api().pathUrl(event.getGuild().getIdLong(), "settings/edit/scan");
        event.reply(context.localize("command.scan.start.message"))
                .addComponents(ActionRow.of(Button.link(url, context.localize("command.scan.start.button"))))
                .setEphemeral(true)
                .complete();
        scanService.scan(event.getGuild());
    }
}
