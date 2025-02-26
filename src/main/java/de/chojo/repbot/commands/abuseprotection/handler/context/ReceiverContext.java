/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.abuseprotection.handler.context;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.Text;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Handles the receiver context command for abuse protection.
 */
public class ReceiverContext implements SlashHandler {
    private final Guilds guilds;

    /**
     * Constructs a ReceiverContext handler with the specified guilds provider.
     *
     * @param guilds the guilds provider
     */
    public ReceiverContext(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Handles the slash command interaction event.
     *
     * @param event the slash command interaction event
     * @param context the event context
     */
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild = guilds.guild(event.getGuild());
        var abuseSettings = guild.settings().abuseProtection();
        if (event.getOptions().isEmpty()) {
            event.reply(Text.getBooleanMessage(context, abuseSettings.isReceiverContext(),
                         "command.abuseprotection.context.receiver.message.true", "command.abuseprotection.context.receiver.message.false"))
                 .queue();
            return;
        }
        var state = event.getOption("state").getAsBoolean();

        event.reply(Text.getBooleanMessage(context, abuseSettings.receiverContext(state),
                     "command.abuseprotection.context.receiver.message.true", "command.abuseprotection.context.receiver.message.false"))
             .queue();
    }
}
