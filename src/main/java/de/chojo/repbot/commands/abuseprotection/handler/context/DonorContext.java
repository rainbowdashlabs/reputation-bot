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
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class DonorContext implements SlashHandler {
    private final GuildRepository guildRepository;

    public DonorContext(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guild = guildRepository.guild(event.getGuild());
        var abuseSettings = guild.settings().abuseProtection();
        if (event.getOptions().isEmpty()) {
            event.reply(Text.getBooleanMessage(context, abuseSettings.isDonorContext(),
                         "command.abuseprotection.context.donor.message.true", "command.abuseprotection.context.donor.message.false"))
                 .queue();
            return;
        }
        var state = event.getOption("state").getAsBoolean();

        event.reply(Text.getBooleanMessage(context, abuseSettings.donorContext(state),
                     "command.abuseprotection.context.donor.message.true", "command.abuseprotection.context.donor.message.false"))
             .queue();

    }
}
