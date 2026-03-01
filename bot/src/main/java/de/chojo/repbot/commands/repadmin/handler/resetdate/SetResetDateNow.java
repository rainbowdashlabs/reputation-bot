/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.repadmin.handler.resetdate;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SetResetDateNow implements SlashHandler {
    private final GuildRepository guildRepository;

    public SetResetDateNow(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        guildRepository.guild(event.getGuild()).settings().general().resetDateNow();

        event.reply(WebPromo.promoString(context) + context.localize("command.repadmin.resetdate.now.message.set"))
                .setEphemeral(true)
                .complete();
    }
}
