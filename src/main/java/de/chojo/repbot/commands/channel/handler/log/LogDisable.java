/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.channel.handler.log;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class LogDisable implements SlashHandler {
    private final GuildRepository guildRepository;

    public LogDisable(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        guildRepository.guild(event.getGuild()).settings().logChannel().active(false);
        event.reply(WebPromo.promoString(context) + context.localize("command.channel.log.disable.message.disabled"))
                .setEphemeral(true)
                .complete();
    }
}
