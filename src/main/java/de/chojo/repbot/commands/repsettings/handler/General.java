/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.repsettings.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.menus.EntryContext;
import de.chojo.jdautil.menus.MenuAction;
import de.chojo.jdautil.menus.entries.MenuEntry;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.util.WebPromo;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.slf4j.Logger;

import java.awt.*;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class General implements SlashHandler {
    private static final Logger log = getLogger(General.class);
    private final GuildRepository guildRepository;

    public General(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guildSettings = guildRepository.guild(event.getGuild()).settings();
        var settings = StringSelectMenu.create("settings")
                                       .setPlaceholder("command.repsettings.general.message.choose")
                                       .setRequiredRange(1, 1)
                                       .addOption("command.repsettings.general.message.option.byreaction.name", "reactions", "command.repsettings.general.message.option.byreaction.description")
                                       .addOption("command.repsettings.general.message.option.byanswer.name", "answers", "command.repsettings.general.message.option.byanswer.description")
                                       .addOption("command.repsettings.general.message.option.bymention.name", "mention", "command.repsettings.general.message.option.bymention.description")
                                       .addOption("command.repsettings.general.message.option.byfuzzy.name", "fuzzy", "command.repsettings.general.message.option.byfuzzy.description")
                                       .addOption("command.repsettings.general.message.option.byembed.name", "embed", "command.repsettings.general.message.option.byembed.description")
                                       .addOption("command.repsettings.general.message.option.bycommand.name", "command", "command.repsettings.general.message.option.bycommand.description")
                                       .addOption("command.repsettings.general.message.option.skipsingletarget.name", "directembed", "command.repsettings.general.message.option.skipsingletarget.description")
                                       .addOption("command.repsettings.general.message.option.reputationmode.name", "reputationmode", "command.repsettings.general.message.option.reputationmode.description")
                                       .build();
        var reactions = getMenu("reactions",
                "command.repsettings.general.message.option.byreaction.description",
                "command.repsettings.general.message.reactions.true",
                "command.repsettings.general.message.reactions.false",
                guildSettings.reputation().isReactionActive());
        var answers = getMenu("answers",
                "command.repsettings.general.message.option.byanswer.description",
                "command.repsettings.general.message.answer.true",
                "command.repsettings.general.message.answer.false",
                guildSettings.reputation().isAnswerActive());
        var mention = getMenu("mention",
                "command.repsettings.general.message.option.bymention.description",
                "command.repsettings.general.message.mention.true",
                "command.repsettings.general.message.mention.false",
                guildSettings.reputation().isMentionActive());
        var fuzzy = getMenu("fuzzy",
                "command.repsettings.general.message.option.byfuzzy.description",
                "command.repsettings.general.message.fuzzy.true",
                "command.repsettings.general.message.fuzzy.false",
                guildSettings.reputation().isFuzzyActive());
        var embed = getMenu("embed",
                "command.repsettings.general.message.option.byembed.description",
                "command.repsettings.general.message.embed.true",
                "command.repsettings.general.message.embed.false",
                guildSettings.reputation().isEmbedActive());
        var command = getMenu("command",
                "command.repsettings.general.message.option.bycommand.description",
                "command.repsettings.general.message.bycommand.true",
                "command.repsettings.general.message.bycommand.false",
                guildSettings.reputation().isCommandActive());
        var skipSingleEmbed = getMenu("directembed",
                "command.repsettings.general.message.option.skipsingletarget.description",
                "command.repsettings.general.message.skipsingleembed.true",
                "command.repsettings.general.message.skipsingleembed.false",
                guildSettings.reputation().isDirectActive());
        var reputationMode = StringSelectMenu.create("reputationmode")
                                             .setPlaceholder("command.repsettings.general.message.option.reputationmode.description")
                                             .setRequiredRange(1, 1)
                                             .addOption(ReputationMode.TOTAL.localeCode(), ReputationMode.TOTAL.name(), "command.repsettings.general.message.reputationMode.total")
                                             .addOption(ReputationMode.ROLLING_MONTH.localeCode(), ReputationMode.ROLLING_MONTH.name(), "command.repsettings.general.message.reputationMode.rollingMonth")
                                             .addOption(ReputationMode.ROLLING_WEEK.localeCode(), ReputationMode.ROLLING_WEEK.name(), "command.repsettings.general.message.reputationMode.rollingWeek")
                                             .addOption(ReputationMode.WEEK.localeCode(), ReputationMode.WEEK.name(), "command.repsettings.general.message.reputationMode.week")
                                             .addOption(ReputationMode.MONTH.localeCode(), ReputationMode.MONTH.name(), "command.repsettings.general.message.reputationMode.month")
                                             .setDefaultValues(Collections.singletonList(guildSettings.general().reputationMode().name()))
                                             .build();

        context.registerMenu(MenuAction.forCallback(getSettings(context, guildSettings), event)
                                       .addComponent(MenuEntry.of(settings, ctx -> {
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
                                       .addComponent(MenuEntry.of(reactions, ctx -> refresh(ctx, res -> guildSettings.reputation()
                                                                                                                     .reactionActive(res), context, guildSettings))
                                                              .hidden())
                                       .addComponent(MenuEntry.of(answers, ctx -> refresh(ctx, res -> guildSettings.reputation()
                                                                                                                   .answerActive(res), context, guildSettings))
                                                              .hidden())
                                       .addComponent(MenuEntry.of(mention, ctx -> refresh(ctx, res -> guildSettings.reputation()
                                                                                                                   .mentionActive(res), context, guildSettings))
                                                              .hidden())
                                       .addComponent(MenuEntry.of(fuzzy, ctx -> refresh(ctx, res -> guildSettings.reputation()
                                                                                                                 .fuzzyActive(res), context, guildSettings))
                                                              .hidden())
                                       .addComponent(MenuEntry.of(embed, ctx -> refresh(ctx, res -> guildSettings.reputation()
                                                                                                                 .embedActive(res), context, guildSettings))
                                                              .hidden())
                                       .addComponent(MenuEntry.of(command, ctx -> refresh(ctx, res -> {
                                                                  guildSettings.reputation().commandActive(res);
                                                                  // The command needs to be hidden or enabled additionally
                                                                  CompletableFuture.runAsync(() -> context.interactionHub().refreshGuildCommands(event.getGuild()))
                                                                                   .exceptionally(err -> {
                                                                                       log.error("Error during command refresh", err);
                                                                                       return null;
                                                                                   });
                                                              }, context, guildSettings))
                                                              .hidden())
                                       .addComponent(MenuEntry.of(skipSingleEmbed, ctx -> refresh(ctx, res -> guildSettings.reputation()
                                                                                                                           .directActive(res), context, guildSettings))
                                                              .hidden())
                                       .addComponent(MenuEntry.of(reputationMode, ctx -> {
                                           var value = ctx.event().getValues().get(0);
                                           var copy = ctx.entry().component().createCopy();
                                           var mode = ReputationMode.valueOf(value);
                                           mode = guildSettings.general().reputationMode(mode);
                                           copy.setDefaultValues(Collections.singleton(mode.name()));
                                           var settingsEmbed = getSettings(context, guildSettings);
                                           ctx.entry().component(copy.build());
                                           ctx.refresh(settingsEmbed);
                                       }).hidden())
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

    private void refresh(EntryContext<StringSelectInteractionEvent, StringSelectMenu> ctx, Consumer<Boolean> result, EventContext
            context, Settings guildSettings) {
        var value = ctx.event().getValues().get(0);
        var copy = ctx.entry().component().createCopy();
        copy.setDefaultValues(Collections.singleton(value));
        result.accept("enabled".equals(value));
        var settings = getSettings(context, guildSettings);
        ctx.entry().component(copy.build());
        ctx.refresh(settings);
    }

    private MessageEmbed getSettings(EventContext context, Settings guildSettings) {
        var messageSettings = guildSettings.reputation();

        return new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.repsettings.general.message.title")
                .appendDescription(WebPromo.promoString(context) + "\n\n")
                .appendDescription(messageSettings.toLocalizedString())
                .setColor(Color.GREEN)
                .build();
    }
}
