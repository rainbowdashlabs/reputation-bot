package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GeneralSettings;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.data.wrapper.MessageSettings;
import de.chojo.repbot.util.EmojiDebug;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.PropertyKey;

import javax.sql.DataSource;
import java.awt.Color;
import java.util.List;

public class RepSettings extends SimpleCommand {
    private final GuildData guildData;
    private final Localizer loc;

    public RepSettings(DataSource source, Localizer localizer) {
        super("repsettings",
                new String[]{"rs"},
                "command.repSettings.description",
                subCommandBuilder()
                        .add("info", "command.repSettings.sub.info", argsBuilder()
                                .build()
                        )
                        .add("reactions", "command.repSettings.sub.reactions", argsBuilder()
                                .add(OptionType.BOOLEAN, "reactions", "reactions")
                                .build()
                        )
                        .add("answer", "command.repSettings.sub.answer", argsBuilder()
                                .add(OptionType.BOOLEAN, "answer", "answer")
                                .build()
                        )
                        .add("mention", "command.repSettings.sub.mention", argsBuilder()
                                .add(OptionType.BOOLEAN, "mention", "mention")
                                .build()
                        )
                        .add("fuzzy", "command.repSettings.sub.fuzzy", argsBuilder()
                                .add(OptionType.BOOLEAN, "fuzzy", "fuzzy").build()
                        )
                        .add("embed", "command.repSettings.sub.embed", argsBuilder()
                                .add(OptionType.BOOLEAN, "embed", "embed").build()
                        )
                        .add("emojidebug", "command.repSettings.sub.emojidebug", argsBuilder()
                                .add(OptionType.BOOLEAN, "active", "active")
                                .build()
                        )
                        .build(),
                Permission.MANAGE_SERVER);
        guildData = new GuildData(source);
        loc = localizer;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) {
            return false;
        }

        var guildSettings = guildData.getGuildSettings(eventWrapper.getGuild());
        var subcmd = context.argString(0).get();
        if ("info".equalsIgnoreCase(subcmd)) {
            return sendSettings(eventWrapper, guildSettings);
        }

        if ("reactions".equalsIgnoreCase(subcmd)) {
            return reactions(eventWrapper, context.subContext(subcmd), guildSettings);
        }

        if ("answer".equalsIgnoreCase(subcmd)) {
            return answer(eventWrapper, context.subContext(subcmd), guildSettings);
        }

        if ("mention".equalsIgnoreCase(subcmd)) {
            return mention(eventWrapper, context.subContext(subcmd), guildSettings);
        }

        if ("fuzzy".equalsIgnoreCase(subcmd)) {
            return fuzzy(eventWrapper, context.subContext(subcmd), guildSettings);
        }

        if ("embed".equalsIgnoreCase(subcmd)) {
            return embed(eventWrapper, context.subContext(subcmd), guildSettings);
        }

        if ("emojidebug".equalsIgnoreCase(subcmd)) {
            return emojidebug(eventWrapper, context.subContext(subcmd), guildSettings);
        }
        return false;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        var guildSettings = guildData.getGuildSettings(event.getGuild());

        var subcmd = event.getSubcommandName();
        if ("info".equalsIgnoreCase(subcmd)) {
            sendSettings(event, guildSettings);
        }

        if ("reactions".equalsIgnoreCase(subcmd)) {
            reactions(event, guildSettings);
        }

        if ("answer".equalsIgnoreCase(subcmd)) {
            answer(event, guildSettings);
        }

        if ("mention".equalsIgnoreCase(subcmd)) {
            mention(event, guildSettings);
        }

        if ("fuzzy".equalsIgnoreCase(subcmd)) {
            fuzzy(event, guildSettings);
        }

        if ("embed".equalsIgnoreCase(subcmd)) {
            embed(event, guildSettings);
        }

        if ("emojidebug".equalsIgnoreCase(subcmd)) {
            emojidebug(event, guildSettings);
        }
    }

    private boolean sendSettings(MessageEventWrapper eventWrapper, GuildSettings guildSettings) {
        eventWrapper.reply(getSettings(eventWrapper.getGuild(), guildSettings)).queue();
        return true;
    }

    private void sendSettings(SlashCommandEvent event, GuildSettings guildSettings) {
        event.replyEmbeds(getSettings(event.getGuild(), guildSettings)).queue();
    }

    private boolean fuzzy(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        var messageSettings = guildSettings.messageSettings();
        if (context.argsEmpty()) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), messageSettings.isFuzzyActive(),
                    "command.repSettings.sub.fuzzy.true", "command.repSettings.sub.fuzzy.false")).queue();
            return true;
        }
        var optFuzzy = context.argBoolean(0);

        if (optFuzzy.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.notABoolean",
                    Replacement.create("INPUT", context.argString(0))), 30);
            return false;
        }

        messageSettings.fuzzyActive(optFuzzy.get());
        if (guildData.updateMessageSettings(eventWrapper.getGuild(), messageSettings)) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), optFuzzy.get(),
                    "command.repSettings.sub.fuzzy.true", "command.repSettings.sub.fuzzy.false")).queue();
        }
        return true;
    }

    private void fuzzy(SlashCommandEvent event, GuildSettings guildSettings) {
        var messageSettings = guildSettings.messageSettings();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(event.getGuild(), messageSettings.isFuzzyActive(),
                    "command.repSettings.sub.fuzzy.true", "command.repSettings.sub.fuzzy.false")).queue();
            return;
        }
        var fuzzy = event.getOption("fuzzy").getAsBoolean();

        messageSettings.fuzzyActive(fuzzy);
        if (guildData.updateMessageSettings(event.getGuild(), messageSettings)) {
            event.reply(getBooleanMessage(event.getGuild(), fuzzy,
                    "command.repSettings.sub.fuzzy.true", "command.repSettings.sub.fuzzy.false")).queue();
        }
    }

    private boolean mention(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        var messageSettings = guildSettings.messageSettings();
        if (context.argsEmpty()) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), messageSettings.isAnswerActive(),
                    "command.repSettings.sub.mention.true", "command.repSettings.sub.mention.false")).queue();
            return true;
        }
        var optMention = context.argBoolean(0);

        if (optMention.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.notABoolean",
                    Replacement.create("INPUT", context.argString(0))), 30);
            return false;
        }

        messageSettings.mentionActive(optMention.get());
        if (guildData.updateMessageSettings(eventWrapper.getGuild(), messageSettings)) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), optMention.get(),
                    "command.repSettings.sub.mention.true", "command.repSettings.sub.mention.false")).queue();
        }
        return true;
    }

    private boolean mention(SlashCommandEvent event, GuildSettings guildSettings) {
        var messageSettings = guildSettings.messageSettings();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(event.getGuild(), messageSettings.isAnswerActive(),
                    "command.repSettings.sub.mention.true", "command.repSettings.sub.mention.false")).queue();
            return true;
        }
        var mention = event.getOption("mention").getAsBoolean();

        messageSettings.mentionActive(mention);
        if (guildData.updateMessageSettings(event.getGuild(), messageSettings)) {
            event.reply(getBooleanMessage(event.getGuild(), mention,
                    "command.repSettings.sub.mention.true", "command.repSettings.sub.mention.false")).queue();
        }
        return true;
    }

    private boolean answer(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        var messageSettings = guildSettings.messageSettings();
        if (context.argsEmpty()) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), messageSettings.isAnswerActive(),
                    "command.repSettings.sub.answer.true", "command.repSettings.sub.answer.false")).queue();
            return true;
        }
        var optAnswer = context.argBoolean(0);

        if (optAnswer.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.notABoolean",
                    Replacement.create("INPUT", context.argString(0))), 30);
            return false;
        }

        messageSettings.answerActive(optAnswer.get());
        if (guildData.updateMessageSettings(eventWrapper.getGuild(), messageSettings)) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), optAnswer.get(),
                    "command.repSettings.sub.answer.true", "command.repSettings.sub.answer.false")).queue();
        }
        return true;
    }

    private void answer(SlashCommandEvent event, GuildSettings guildSettings) {
        var messageSettings = guildSettings.messageSettings();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(event.getGuild(), messageSettings.isAnswerActive(),
                    "command.repSettings.sub.answer.true", "command.repSettings.sub.answer.false")).queue();
            return;
        }
        var answer = event.getOption("answer").getAsBoolean();

        messageSettings.answerActive(answer);
        if (guildData.updateMessageSettings(event.getGuild(), messageSettings)) {
            event.reply(getBooleanMessage(event.getGuild(), answer,
                    "command.repSettings.sub.answer.true", "command.repSettings.sub.answer.false")).queue();
        }
    }

    private boolean embed(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        var messageSettings = guildSettings.messageSettings();
        if (context.argsEmpty()) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), messageSettings.isEmbedActive(),
                    "command.repSettings.sub.embed.true", "command.repSettings.sub.embed.false")).queue();
            return true;
        }
        var optEmbed = context.argBoolean(0);

        if (optEmbed.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.notABoolean",
                    Replacement.create("INPUT", context.argString(0))), 30);
            return false;
        }

        messageSettings.embedActive(optEmbed.get());
        if (guildData.updateMessageSettings(eventWrapper.getGuild(), messageSettings)) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), optEmbed.get(),
                    "command.repSettings.sub.embed.true", "command.repSettings.sub.embed.false")).queue();
        }
        return true;
    }

    private void embed(SlashCommandEvent event, GuildSettings guildSettings) {
        var messageSettings = guildSettings.messageSettings();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(event.getGuild(), messageSettings.isAnswerActive(),
                    "command.repSettings.sub.embed.true", "command.repSettings.sub.embed.false")).queue();
            return;
        }
        var embed = event.getOption("embed").getAsBoolean();

        messageSettings.embedActive(embed);
        if (guildData.updateMessageSettings(event.getGuild(), messageSettings)) {
            event.reply(getBooleanMessage(event.getGuild(), embed,
                    "command.repSettings.sub.embed.true", "command.repSettings.sub.embed.false")).queue();
        }
    }

    private boolean reactions(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        var messageSettings = guildSettings.messageSettings();
        if (context.argsEmpty()) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), messageSettings.isReactionActive(),
                    "command.repSettings.sub.reactions.true", "command.repSettings.sub.reactions.false")).queue();
            return true;
        }
        var optReactions = context.argBoolean(0);

        if (optReactions.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.notABoolean",
                    Replacement.create("INPUT", context.argString(0))), 30);
            return false;
        }

        messageSettings.reactionActive(optReactions.get());
        if (guildData.updateMessageSettings(eventWrapper.getGuild(), messageSettings)) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), optReactions.get(),
                    "command.repSettings.sub.reactions.true", "command.repSettings.sub.reactions.false")).queue();
        }
        return true;
    }

    private void reactions(SlashCommandEvent event, GuildSettings guildSettings) {
        var messageSettings = guildSettings.messageSettings();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(event.getGuild(), messageSettings.isReactionActive(),
                    "command.repSettings.sub.reactions.true", "command.repSettings.sub.reactions.false")).queue();
            return;
        }
        var reactions = event.getOption("reactions").getAsBoolean();

        messageSettings.reactionActive(reactions);
        if (guildData.updateMessageSettings(event.getGuild(), messageSettings)) {
            event.reply(getBooleanMessage(event.getGuild(), reactions,
                    "command.repSettings.sub.reactions.true", "command.repSettings.sub.reactions.false")).queue();
        }
    }

    private boolean emojidebug(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        var generalSettings = guildSettings.generalSettings();
        if (context.argsEmpty()) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), generalSettings.isEmojiDebug(),
                    "command.repSettings.sub.emojidebug.true", "command.repSettings.sub.emojidebug.false")
                               + "\n" + loc.localize(emojiExplanation(), eventWrapper.getGuild())).queue();
            return true;
        }
        var optEmojiDebug = context.argBoolean(0);

        if (optEmojiDebug.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.notABoolean",
                    Replacement.create("INPUT", context.argString(0))), 30);
            return false;
        }

        if (guildData.setEmojiDebug(eventWrapper.getGuild(), optEmojiDebug.get())) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), optEmojiDebug.get(),
                    "command.repSettings.sub.emojidebug.true", "command.repSettings.sub.emojidebug.false")).queue();
        }
        return true;
    }

    private void emojidebug(SlashCommandEvent event, GuildSettings guildSettings) {
        var generalSettings = guildSettings.generalSettings();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(event.getGuild(), generalSettings.isEmojiDebug(),
                    "command.repSettings.sub.emojidebug.true", "command.repSettings.sub.emojidebug.false")
                        + "\n" + loc.localize(emojiExplanation(), event.getGuild())).queue();
            return;
        }
        var emojidebug = event.getOption("active").getAsBoolean();

        if (guildData.setEmojiDebug(event.getGuild(), emojidebug)) {
            event.reply(getBooleanMessage(event.getGuild(), emojidebug,
                    "command.repSettings.sub.emojidebug.true", "command.repSettings.sub.emojidebug.false")).queue();
        }
    }

    private MessageEmbed getSettings(Guild guild, GuildSettings guildSettings) {
        var messageSettings = guildSettings.messageSettings();
        var setting = List.of(
                getSetting("command.repSettings.embed.descr.byReaction", messageSettings.isReactionActive()),
                getSetting("command.repSettings.embed.descr.byAnswer", messageSettings.isAnswerActive()),
                getSetting("command.repSettings.embed.descr.byMention", messageSettings.isMentionActive()),
                getSetting("command.repSettings.embed.descr.byFuzzy", messageSettings.isFuzzyActive()),
                getSetting("command.repSettings.embed.descr.byEmbed", messageSettings.isEmbedActive()),
                getSetting("command.repSettings.embed.descr.emojidebug", guildSettings.generalSettings().isEmojiDebug())
        );

        var settings = String.join("\n", setting);

        return new LocalizedEmbedBuilder(loc, guild)
                .setTitle("command.repSettings.embed.title")
                .appendDescription(loc.localize(settings, guild))
                .setColor(Color.GREEN)
                .build();
    }

    private String getSetting(@PropertyKey(resourceBundle = "locale") String locale, Object object) {
        return String.format("$%s$: %s", locale, object);
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
                emojiString(EmojiDebug.PROMPTED, "command.repSettings.sub.emojidebug.explain.prompted")
        );
        return String.join("\n", emojis);
    }

    private String emojiString(String emoji, @PropertyKey(resourceBundle = "locale") String code) {
        return String.format("%s ➜ $%s$", emoji, code);
    }

    private String getBooleanMessage(Guild guild, boolean value, String whenTrue, String whenFalse) {
        return loc.localize(value ? whenTrue : whenFalse, guild);
    }
}
