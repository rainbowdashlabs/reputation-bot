/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.bot.handler.entitlement;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.TestEntitlementCreateAction;

import java.util.List;
import java.util.stream.Collectors;

public class Create implements SlashHandler {
    private final Configuration configuration;

    public Create(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext eventContext) {
        Long ownerid = event.getOption("ownerid", OptionMapping::getAsLong);
        if (ownerid == null) {
            ownerid = event.getGuild().getIdLong();
        }
        event.deferReply(true).queue();
        Entitlement complete = event.getJDA()
                                    .createTestEntitlement(event.getOption("sku", OptionMapping::getAsLong),
                                            ownerid,
                                            TestEntitlementCreateAction.OwnerType.GUILD_SUBSCRIPTION)
                                    .complete();
        event.getHook().editOriginal("Entitlement granted").queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        switch (event.getFocusedOption().getName()) {
            case "sku" -> {
                var choices = configuration.skus().subscriptions().stream().map(sku -> new Command.Choice(sku.name(), String.valueOf(sku.subscriptionSku()))).toList();
                event.replyChoices(choices).queue();
            }
            default -> event.replyChoices().queue();
        }
    }
}
