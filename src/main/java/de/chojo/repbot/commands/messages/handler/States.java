/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.messages.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.menus.EntryContext;
import de.chojo.jdautil.menus.MenuAction;
import de.chojo.jdautil.menus.entries.MenuEntry;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.awt.*;
import java.util.Collections;
import java.util.function.Consumer;

public class States implements SlashHandler {
    private final GuildRepository guildRepository;

    public States(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var settings = guildRepository.guild(event.getGuild()).settings();
        var setting = StringSelectMenu.create("setting")
                                      .setPlaceholder("command.messages.states.message.choose")
                                      .setRequiredRange(1, 1)
                                      .addOption("command.messages.states.message.option.reactionconfirmation.name", "reaction_confirmation", "command.messages.states.message.option.reactionConfirmation.description")
                                      .addOption("command.messages.states.message.option.commandreputationephemeral.name", "command_reply", "command.messages.states.message.option.commandreputationephemeral.description")
                                      .build();
        var reactions = getMenu("reaction_confirmation",
                "command.messages.states.message.option.reactionconfirmation.name",
                "command.messages.states.message.choice.reactionConfirmation.true",
                "command.messages.states.message.choice.reactionConfirmation.false",
                settings.messages().isReactionConfirmation());
        var commandreputationephemeral = getMenu("command_reply",
                "command.messages.states.message.option.commandreputationephemeral.name",
                "command.messages.states.message.choice.commandreputationephemeral.true",
                "command.messages.states.message.choice.commandreputationephemeral.false",
                settings.messages().isCommandReputationEphemeral());

        context.registerMenu(MenuAction.forCallback(getSettings(context, settings), event)
                                       .addComponent(MenuEntry.of(setting, ctx -> {
                                           var option = ctx.event().getValues().get(0);
                                           var entry = ctx.container().entry(option).get();
                                           ctx.container().entries().forEach(MenuEntry::hidden);
                                           ctx.entry().visible(true);
                                           entry.visible(true);
                                           var copy = ctx.entry().component().createCopy();
                                           copy.setDefaultValues(Collections.singleton(option));
                                           ctx.entry().component(copy.build());
                                           ctx.refresh();
                                       }))
                                       .addComponent(MenuEntry.of(reactions, ctx -> refresh(ctx, res -> settings.messages()
                                                                                                                .reactionConfirmation(res), context, settings))
                                                              .hidden())
                                       .addComponent(MenuEntry.of(commandreputationephemeral, ctx -> refresh(ctx, res -> settings.messages()
                                                                                                                                 .commandReputationEphemeral(res), context, settings))
                                                              .hidden())
                                       .asEphemeral()
                                       .build());
    }

    private StringSelectMenu getMenu(String id, String placeholder, String enabledDescr, String disabledDescr, boolean state) {
        return StringSelectMenu.create(id)
                               .setPlaceholder(placeholder)
                               .setRequiredRange(1, 1)
                               .addOption("words.enabled", "enabled", enabledDescr)
                               .addOption("words.disabled", "disabled", disabledDescr)
                               .setDefaultValues(Collections.singleton(state ? "enabled" : "disabled"))
                               .build();
    }

    private void refresh(EntryContext<StringSelectInteractionEvent, StringSelectMenu> ctx, Consumer<Boolean> result, EventContext context, Settings guildSettings) {
        var value = ctx.event().getValues().get(0);
        var copy = ctx.entry().component().createCopy();
        copy.setDefaultValues(Collections.singleton(value));
        result.accept("enabled".equals(value));
        var settings = getSettings(context, guildSettings);
        ctx.entry().component(copy.build());
        ctx.refresh(settings);
    }

    private MessageEmbed getSettings(EventContext context, Settings guildSettings) {
        var messages = guildSettings.messages();

        return new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.messages.states.message.title")
                .appendDescription(WebPromo.promoString(context) + "\n\n")
                .appendDescription(messages.toLocalizedString())
                .setColor(Color.GREEN)
                .build();
    }
}
