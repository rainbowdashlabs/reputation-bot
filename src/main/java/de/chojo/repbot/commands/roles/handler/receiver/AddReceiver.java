/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.roles.handler.receiver;
import de.chojo.repbot.util.WebPromo;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;

public class AddReceiver implements SlashHandler {
    private final GuildRepository guildRepository;

    public AddReceiver(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var role = event.getOption("role").getAsRole();
        guildRepository.guild(event.getGuild()).settings().thanking().receiverRoles().add(role);
        event.reply(WebPromo.promoString(context) + "\n" + context.localize("command.roles.receiver.add.message.add",
                Replacement.createMention(role))).setAllowedMentions(Collections.emptyList())
             .setEphemeral(true)
             .complete();
    }
}
