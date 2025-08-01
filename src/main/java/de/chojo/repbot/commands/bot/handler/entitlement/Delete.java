/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler.entitlement;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class Delete implements SlashHandler {
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent slash, EventContext eventContext) {
        slash.getJDA().deleteTestEntitlement(slash.getOption("entitlementid", OptionMapping::getAsLong)).complete();
        slash.reply("Deleted entitlement").complete();
    }
}
