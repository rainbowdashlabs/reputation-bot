/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel.handler.autopost;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.service.AutopostService;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class AutopostRefresh implements SlashHandler {
    private final AutopostService autopostService;
    private final GuildRepository guildRepository;

    public AutopostRefresh(AutopostService autopostService, GuildRepository guildRepository) {
        this.autopostService = autopostService;
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        if (!guildRepository.guild(event.getGuild()).settings().autopost().active()) {
            event.reply(WebPromo.promoString(context) + "command.channel.autopost.refresh.message.inactive")
                 .setEphemeral(true)
                 .complete();
            return;
        }
        autopostService.update(event.getGuild());
        event.reply(WebPromo.promoString(context) + "command.channel.autopost.refresh.message.refreshed")
             .setEphemeral(true)
             .complete();
    }
}
