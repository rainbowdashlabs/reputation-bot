/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.abuseprotection.handler.context;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.Text;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ReceiverContext implements SlashHandler {
    private final GuildRepository guildRepository;

    public ReceiverContext(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild = guildRepository.guild(event.getGuild());
        var abuseSettings = guild.settings().abuseProtection();
        if (event.getOptions().isEmpty()) {
            event.reply(WebPromo.promoString(context)  + Text.getBooleanMessage(context, abuseSettings.isReceiverContext(),
                         "command.abuseprotection.context.receiver.message.true", "command.abuseprotection.context.receiver.message.false"))
                 .setEphemeral(true)
                 .complete();
            return;
        }
        var state = event.getOption("state").getAsBoolean();

        event.reply(WebPromo.promoString(context) + "\n" + Text.getBooleanMessage(context, abuseSettings.receiverContext(state),
                     "command.abuseprotection.context.receiver.message.true", "command.abuseprotection.context.receiver.message.false"))
             .setEphemeral(true)
             .complete();
    }
}
