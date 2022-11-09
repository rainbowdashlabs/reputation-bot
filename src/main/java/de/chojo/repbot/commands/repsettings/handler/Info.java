package de.chojo.repbot.commands.repsettings.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.menus.EntryContext;
import de.chojo.jdautil.menus.MenuAction;
import de.chojo.jdautil.menus.entries.MenuEntry;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.Color;
import java.util.Collections;
import java.util.function.Consumer;

public class Info implements SlashHandler {
    private final Guilds guilds;

    public Info(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var guildSettings = guilds.guild(event.getGuild()).settings();
        var settings = StringSelectMenu.create("settings")
                .setPlaceholder("command.repsettings.info.message.choose")
                .setRequiredRange(1, 1)
                .addOption("command.repsettings.info.message.option.byreaction.name", "reactions", "command.repsettings.info.message.option.byreaction.description")
                .addOption("command.repsettings.info.message.option.byanswer.name", "answers", "command.repsettings.info.message.option.byanswer.description")
                .addOption("command.repsettings.info.message.option.bymention.name", "mention", "command.repsettings.info.message.option.bymention.description")
                .addOption("command.repsettings.info.message.option.byfuzzy.name", "fuzzy", "command.repsettings.info.message.option.byfuzzy.description")
                .addOption("command.repsettings.info.message.option.byembed.name", "embed", "command.repsettings.info.message.option.byembed.description")
                .addOption("command.repsettings.info.message.option.emojidebug.name", "emojidebug", "command.repsettings.info.message.option.emojidebug.description")
                .addOption("command.repsettings.info.message.option.skipsingleembed.name", "directembed", "command.repsettings.info.message.option.skipsingleembed.description")
                .addOption("command.repsettings.info.message.option.reputationmode.name", "reputationmode", "command.repsettings.info.message.option.reputationmode.description")
                .build();
        var reactions = getMenu("reactions",
                "command.repsettings.info.message.option.byreaction.description",
                "command.repsettings.info.message.reactions.true",
                "command.repsettings.info.message.reactions.false",
                guildSettings.reputation().isReactionActive());
        var answers = getMenu("answers",
                "command.repsettings.info.message.option.byanswer.description",
                "command.repsettings.info.message.answer.true",
                "command.repsettings.info.message.answer.false",
                guildSettings.reputation().isAnswerActive());
        var mention = getMenu("mention",
                "command.repsettings.info.message.option.bymention.description",
                "command.repsettings.info.message.mention.true",
                "command.repsettings.info.message.mention.false",
                guildSettings.reputation().isMentionActive());
        var fuzzy = getMenu("fuzzy",
                "command.repsettings.info.message.option.byfuzzy.description",
                "command.repsettings.info.message.fuzzy.true",
                "command.repsettings.info.message.fuzzy.false",
                guildSettings.reputation().isFuzzyActive());
        var embed = getMenu("embed",
                "command.repsettings.info.message.option.byembed.description",
                "command.repsettings.info.message.embed.true",
                "command.repsettings.info.message.embed.false",
                guildSettings.reputation().isEmbedActive());
        var emojidebug = getMenu("emojidebug",
                "command.repsettings.info.message.option.emojidebug.description",
                "command.repsettings.emojidebug.message.true",
                "command.repsettings.emojidebug.message.false",
                guildSettings.general().isEmojiDebug());
        var skipSingleEmbed = getMenu("directembed",
                "command.repsettings.info.message.option.skipsingleembed.description",
                "command.repsettings.info.message.skipsingleembed.true",
                "command.repsettings.info.message.skipsingleembed.false",
                guildSettings.reputation().isSkipSingleEmbed());
        var reputationMode = StringSelectMenu.create("reputationmode")
                .setPlaceholder("command.repsettings.info.message.option.reputationmode.description")
                .setRequiredRange(1, 1)
                .addOption(ReputationMode.TOTAL.localeCode(), ReputationMode.TOTAL.name(), "command.repsettings.info.message.reputationMode.total")
                .addOption(ReputationMode.ROLLING_MONTH.localeCode(), ReputationMode.ROLLING_MONTH.name(), "command.repsettings.info.message.reputationMode.rollingMonth")
                .addOption(ReputationMode.ROLLING_WEEK.localeCode(), ReputationMode.ROLLING_WEEK.name(), "command.repsettings.info.message.reputationMode.rollingWeek")
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
                                       .addComponent(MenuEntry.of(emojidebug, ctx -> refresh(ctx, res -> guildSettings.general()
                                                                                                                      .emojiDebug(res), context, guildSettings))
                                               .hidden())
                                       .addComponent(MenuEntry.of(skipSingleEmbed, ctx -> refresh(ctx, res -> guildSettings.reputation()
                                                                                                                           .skipSingleEmbed(res), context, guildSettings))
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
                .setTitle("command.repsettings.info.message.title")
                .appendDescription(messageSettings.toLocalizedString())
                .setColor(Color.GREEN)
                .build();
    }
}
