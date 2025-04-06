/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler.entitlement;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.TestEntitlementCreateAction;

import java.util.List;

public class Create implements SlashHandler {
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext eventContext) {
        Entitlement complete = event.getJDA()
                                    .createTestEntitlement(event.getOption("sku", OptionMapping::getAsLong),
                                            event.getOption("ownerid", OptionMapping::getAsLong),
                                            TestEntitlementCreateAction.OwnerType.GUILD_SUBSCRIPTION)
                                    .complete();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        switch (event.getFocusedOption().getName()) {
            case "sku" -> {
                List<Entitlement> complete = event.getJDA().retrieveEntitlements().complete();
                List<Command.Choice> choices = Completion.complete(event.getFocusedOption().getValue(), complete, Entitlement::getSkuId);
                event.replyChoices(choices).queue();
            }
            default -> event.replyChoices().queue();
        }
    }
}
