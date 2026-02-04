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

public class AutopostDisable implements SlashHandler {
    private final GuildRepository guildRepository;
    private final AutopostService autopostService;

    public AutopostDisable(GuildRepository guildRepository, AutopostService autopostService) {
        this.guildRepository = guildRepository;
        this.autopostService = autopostService;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        guildRepository.guild(event.getGuild()).settings().autopost().active(false);
        autopostService.delete(event.getGuild());
        event.reply(WebPromo.promoString(context) + "command.channel.autopost.disable.message.disabled")
             .setEphemeral(true)
             .complete();
    }
}
