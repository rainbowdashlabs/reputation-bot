/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.repsettings.handler.name;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Reset implements SlashHandler {
    private final GuildRepository guildRepository;

    public Reset(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext eventContext) {
        guildRepository.guild(event.getGuild()).localeOverrides().removeOverride("words.reputation");
        event.reply(WebPromo.promoString(eventContext) + "\n"
                        + eventContext.localize("command.repsettings.name.reset.message.reset"))
                .setEphemeral(true)
                .queue();
    }
}
