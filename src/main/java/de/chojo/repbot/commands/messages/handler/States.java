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
import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.access.guild.settings.sub.Messages;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class States implements SlashHandler {
    private static final String ANNOUNCE_DESCRIPTION = "command.messages.states.message.option.announce.description";
    private static final String ANNOUNCE_ENABLED = "command.messages.states.message.choice.announce.true";
    private static final String ANNOUNCE_DISABLED = "command.messages.states.message.choice.announce.false";
    private final GuildRepository guildRepository;

    public States(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var settings = guildRepository.guild(event.getGuild()).settings();
        var states = states(settings.messages());

        var setting = StringSelectMenu.create("setting")
                .setPlaceholder("command.messages.states.message.choose")
                .setRequiredRange(1, 1);
        for (var state : states) {
            setting.addOption(state.name(), state.id(), state.description());
        }

        var menu = MenuAction.forCallback(getSettings(context, settings), event)
                .addComponent(MenuEntry.of(setting.build(), ctx -> {
                    var option = ctx.event().getValues().get(0);
                    var entry = ctx.container().entry(option).get();
                    ctx.container().entries().forEach(MenuEntry::hidden);
                    ctx.entry().visible(true);
                    entry.visible(true);
                    var copy = ctx.entry().component().createCopy();
                    copy.setDefaultValues(Collections.singleton(option));
                    ctx.entry().component(copy.build());
                    ctx.refresh();
                }));

        for (var state : states) {
            menu.addComponent(MenuEntry.of(
                            getMenu(state.id(), state.name(), state.enabled(), state.disabled(), state.state()),
                            ctx -> refresh(ctx, state.apply(), context, settings))
                    .hidden());
        }

        context.registerMenu(menu.asEphemeral().build());
    }

    private List<State> states(Messages messages) {
        return List.of(
                new State(
                        "command_reply",
                        "command.messages.states.message.option.commandreputationephemeral.name",
                        "command.messages.states.message.option.commandreputationephemeral.description",
                        "command.messages.states.message.choice.commandreputationephemeral.true",
                        "command.messages.states.message.choice.commandreputationephemeral.false",
                        messages.isCommandReputationEphemeral(),
                        messages::commandReputationEphemeral),
                announce(ThankType.REACTION, messages.isAnnounceReaction(), messages::announceReaction),
                announce(ThankType.ANSWER, messages.isAnnounceAnswer(), messages::announceAnswer),
                announce(ThankType.MENTION, messages.isAnnounceMention(), messages::announceMention),
                announce(ThankType.FUZZY, messages.isAnnounceFuzzy(), messages::announceFuzzy),
                announce(ThankType.EMBED, messages.isAnnounceEmbed(), messages::announceEmbed),
                announce(ThankType.DIRECT, messages.isAnnounceDirect(), messages::announceDirect),
                announce(ThankType.COMMAND, messages.isAnnounceCommand(), messages::announceCommand),
                new State(
                        "announce_delete",
                        "command.messages.states.message.option.announcedelete.name",
                        "command.messages.states.message.option.announcedelete.description",
                        "command.messages.states.message.choice.announceDelete.true",
                        "command.messages.states.message.choice.announceDelete.false",
                        messages.isAnnounceDelete(),
                        messages::announceDelete));
    }

    private State announce(ThankType type, boolean state, Consumer<Boolean> apply) {
        var id = "announce_%s".formatted(type.name().toLowerCase(Locale.ROOT));
        return new State(
                id, type.nameLocaleKey(), ANNOUNCE_DESCRIPTION, ANNOUNCE_ENABLED, ANNOUNCE_DISABLED, state, apply);
    }

    private StringSelectMenu getMenu(
            String id, String placeholder, String enabledDescr, String disabledDescr, boolean state) {
        return StringSelectMenu.create(id)
                .setPlaceholder(placeholder)
                .setRequiredRange(1, 1)
                .addOption("words.enabled", "enabled", enabledDescr)
                .addOption("words.disabled", "disabled", disabledDescr)
                .setDefaultValues(Collections.singleton(state ? "enabled" : "disabled"))
                .build();
    }

    private void refresh(
            EntryContext<StringSelectInteractionEvent, StringSelectMenu> ctx,
            Consumer<Boolean> result,
            EventContext context,
            Settings guildSettings) {
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

    /**
     * A toggleable message state.
     *
     * @param id          id of the select menu and value of the option selecting it
     * @param name        locale key of the setting name
     * @param description locale key of the setting description
     * @param enabled     locale key describing the enabled state
     * @param disabled    locale key describing the disabled state
     * @param state       current state
     * @param apply       applies a new state
     */
    private record State(
            String id,
            String name,
            String description,
            String enabled,
            String disabled,
            boolean state,
            Consumer<Boolean> apply) {}
}
