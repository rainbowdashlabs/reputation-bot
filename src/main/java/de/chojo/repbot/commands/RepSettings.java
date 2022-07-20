package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.menus.EntryContext;
import de.chojo.jdautil.menus.MenuAction;
import de.chojo.jdautil.menus.entries.MenuEntry;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.EmojiDebug;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.jetbrains.annotations.PropertyKey;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class RepSettings extends SimpleCommand {
    private final Guilds guilds;

    public RepSettings(Guilds guilds) {
        super(CommandMeta.builder("repsettings", "command.repSettings.description")
                .addSubCommand("info", "command.repSettings.sub.info")
                .addSubCommand("emojidebug", "command.repSettings.sub.emojidebug")
                .adminCommand());
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var guildSettings = guilds.guild(event.getGuild()).settings();

        var subcmd = event.getSubcommandName();
        if ("info".equalsIgnoreCase(subcmd)) {
            sendSettings(event, context, guildSettings);
        }

        if ("emojidebug".equalsIgnoreCase(subcmd)) {
            emojidebug(event, context, guildSettings);
        }
    }

    private void sendSettings(SlashCommandInteractionEvent event, SlashCommandContext context, Settings guildSettings) {
        var settings = SelectMenu.create("settings")
                .setPlaceholder("command.repSettings.embed.choose")
                .setRequiredRange(1, 1)
                .addOption("command.repSettings.embed.descr.byReaction", "reactions", "command.repSettings.sub.reactions.arg.reactions")
                .addOption("command.repSettings.embed.descr.byAnswer", "answers", "command.repSettings.sub.answer.arg.answer")
                .addOption("command.repSettings.embed.descr.byMention", "mention", "command.repSettings.sub.mention.arg.mention")
                .addOption("command.repSettings.embed.descr.byFuzzy", "fuzzy", "command.repSettings.sub.fuzzy.arg.fuzzy")
                .addOption("command.repSettings.embed.descr.byEmbed", "embed", "command.repSettings.sub.embed.arg.embed")
                .addOption("command.repSettings.embed.descr.emojidebug", "emojidebug", "command.repSettings.sub.emojidebug.arg.active")
                .build();
        var reactions = getMenu("reactions",
                "command.repSettings.sub.reactions.arg.reactions",
                "command.repSettings.sub.reactions.true",
                "command.repSettings.sub.reactions.false",
                guildSettings.messages().isReactionActive());
        var answers = getMenu("answers",
                "command.repSettings.sub.answer.arg.answer",
                "command.repSettings.sub.answer.true",
                "command.repSettings.sub.answer.false",
                guildSettings.messages().isAnswerActive());
        var mention = getMenu("mention",
                "command.repSettings.sub.mention.arg.mention",
                "command.repSettings.sub.mention.true",
                "command.repSettings.sub.mention.false",
                guildSettings.messages().isMentionActive());
        var fuzzy = getMenu("fuzzy",
                "command.repSettings.sub.fuzzy.arg.fuzzy",
                "command.repSettings.sub.fuzzy.true",
                "command.repSettings.sub.fuzzy.false",
                guildSettings.messages().isFuzzyActive());
        var embed = getMenu("embed",
                "command.repSettings.sub.embed.arg.embed",
                "command.repSettings.sub.embed.true",
                "command.repSettings.sub.embed.false",
                guildSettings.messages().isEmbedActive());
        var emojidebug = getMenu("emojidebug",
                "command.repSettings.sub.emojidebug.arg.active",
                "command.repSettings.sub.emojidebug.true",
                "command.repSettings.sub.emojidebug.false",
                guildSettings.general().isEmojiDebug());

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
                .addComponent(MenuEntry.of(reactions, ctx -> {
                    refresh(ctx, res -> guildSettings.messages().reactionActive(res), context, guildSettings);
                }).hidden())
                .addComponent(MenuEntry.of(answers, ctx -> {
                    refresh(ctx, res -> guildSettings.messages().answerActive(res), context, guildSettings);
                }).hidden())
                .addComponent(MenuEntry.of(mention, ctx -> {
                    refresh(ctx, res -> guildSettings.messages().mentionActive(res), context, guildSettings);
                }).hidden())
                .addComponent(MenuEntry.of(fuzzy, ctx -> {
                    refresh(ctx, res -> guildSettings.messages().fuzzyActive(res), context, guildSettings);
                }).hidden())
                .addComponent(MenuEntry.of(embed, ctx -> {
                    refresh(ctx, res -> guildSettings.messages().embedActive(res), context, guildSettings);
                }).hidden())
                .addComponent(MenuEntry.of(emojidebug, ctx -> {
                    refresh(ctx, res -> guildSettings.general().emojiDebug(res), context, guildSettings);
                }).hidden())
                .asEphemeral()
                .build());
    }

    private SelectMenu getMenu(String id, String placeholder, String enabledDescr, String disabledDescr, boolean state) {
        return SelectMenu.create(id)
                .setPlaceholder(placeholder)
                .setRequiredRange(1, 1)
                .addOption("words.enabled", "enabled", enabledDescr)
                .addOption("words.disabled", "disabled", disabledDescr)
                .setDefaultValues(Collections.singleton(state ? "enabled" : "disabled"))
                .build();
    }

    private void refresh(EntryContext<SelectMenuInteractionEvent, SelectMenu> ctx, Consumer<Boolean> result, SlashCommandContext context, Settings guildSettings) {
        var value = ctx.event().getValues().get(0);
        var copy = ctx.entry().component().createCopy();
        copy.setDefaultValues(Collections.singleton(value));
        result.accept("enabled".equals(value));
        var settings = getSettings(context, guildSettings);
        ctx.entry().component(copy.build());
        ctx.refresh(settings);
    }

    private void emojidebug(SlashCommandInteractionEvent event, SlashCommandContext context, Settings guildSettings) {
        var generalSettings = guildSettings.general();
        event.reply(getBooleanMessage(context, generalSettings.isEmojiDebug(),
                "command.repSettings.sub.emojidebug.true", "command.repSettings.sub.emojidebug.false")
                    + "\n" + context.localize(emojiExplanation())).queue();
    }

    private MessageEmbed getSettings(SlashCommandContext context, Settings guildSettings) {
        var messageSettings = guildSettings.messages();

        return new LocalizedEmbedBuilder(context.localizer())
                .setTitle("command.repSettings.embed.title")
                .appendDescription(messageSettings.toLocalizedString())
                .setColor(Color.GREEN)
                .build();
    }

    private String emojiExplanation() {
        var emojis = List.of(
                String.format("$%s$", "command.repSettings.sub.emojidebug.explain.title"),
                emojiString(EmojiDebug.FOUND_THANKWORD, "command.repSettings.sub.emojidebug.explain.found"),
                emojiString(EmojiDebug.ONLY_COOLDOWN, "command.repSettings.sub.emojidebug.explain.cooldown"),
                emojiString(EmojiDebug.EMPTY_CONTEXT, "command.repSettings.sub.emojidebug.explain.noReceiver"),
                emojiString(EmojiDebug.TARGET_NOT_IN_CONTEXT, "command.repSettings.sub.emojidebug.explain.noRecentMessages"),
                emojiString(EmojiDebug.DONOR_NOT_IN_CONTEXT, "command.repSettings.sub.emojidebug.explain.noDonor"),
                emojiString(EmojiDebug.TOO_OLD, "command.repSettings.sub.emojidebug.explain.tooOld"),
                emojiString(EmojiDebug.PROMPTED, "command.repSettings.sub.emojidebug.explain.prompted"),
                emojiString(EmojiDebug.DONOR_LIMIT, "command.repSettings.sub.emojidebug.explain.donorLimit"),
                emojiString(EmojiDebug.RECEIVER_LIMIT, "command.repSettings.sub.emojidebug.explain.receiverLimit")
        );
        return String.join("\n", emojis);
    }

    private String emojiString(String emoji, @PropertyKey(resourceBundle = "locale") String code) {
        return String.format("%s ➜ $%s$", emoji, code);
    }

    private String getBooleanMessage(SlashCommandContext context, boolean value, String whenTrue, String whenFalse) {
        return context.localize(value ? whenTrue : whenFalse);
    }
}
