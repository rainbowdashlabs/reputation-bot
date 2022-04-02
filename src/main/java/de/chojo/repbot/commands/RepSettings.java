package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.util.EmojiDebug;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.PropertyKey;

import javax.sql.DataSource;
import java.awt.Color;
import java.util.List;

public class RepSettings extends SimpleCommand {
    private final GuildData guildData;

    public RepSettings(DataSource source) {
        super(CommandMeta.builder("repsettings", "command.repSettings.description")
                .addSubCommand("info", "command.repSettings.sub.info")
                .addSubCommand("reactions", "command.repSettings.sub.reactions", argsBuilder()
                        .add(SimpleArgument.bool("reactions", "reactions")))
                .addSubCommand("answer", "command.repSettings.sub.answer", argsBuilder()
                        .add(SimpleArgument.bool("answer", "answer")))
                .addSubCommand("mention", "command.repSettings.sub.mention", argsBuilder()
                        .add(SimpleArgument.bool("mention", "mention")))
                .addSubCommand("fuzzy", "command.repSettings.sub.fuzzy", argsBuilder()
                        .add(SimpleArgument.bool("fuzzy", "fuzzy")))
                .addSubCommand("embed", "command.repSettings.sub.embed", argsBuilder()
                        .add(SimpleArgument.bool("embed", "embed")))
                .addSubCommand("emojidebug", "command.repSettings.sub.emojidebug", argsBuilder()
                        .add(SimpleArgument.bool("active", "active")))
                .withPermission());
        guildData = new GuildData(source);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var guildSettings = guildData.getGuildSettings(event.getGuild());

        var subcmd = event.getSubcommandName();
        if ("info".equalsIgnoreCase(subcmd)) {
            sendSettings(event, context, guildSettings);
        }

        if ("reactions".equalsIgnoreCase(subcmd)) {
            reactions(event, context, guildSettings);
        }

        if ("answer".equalsIgnoreCase(subcmd)) {
            answer(event, context, guildSettings);
        }

        if ("mention".equalsIgnoreCase(subcmd)) {
            mention(event, context, guildSettings);
        }

        if ("fuzzy".equalsIgnoreCase(subcmd)) {
            fuzzy(event, context, guildSettings);
        }

        if ("embed".equalsIgnoreCase(subcmd)) {
            embed(event, context, guildSettings);
        }

        if ("emojidebug".equalsIgnoreCase(subcmd)) {
            emojidebug(event, context, guildSettings);
        }
    }

    private void sendSettings(SlashCommandInteractionEvent event, SlashCommandContext context, GuildSettings guildSettings) {
        event.replyEmbeds(getSettings(context, guildSettings)).queue();
    }

    private void fuzzy(SlashCommandInteractionEvent event, SlashCommandContext context, GuildSettings guildSettings) {
        var messageSettings = guildSettings.messageSettings();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(context, messageSettings.isFuzzyActive(),
                    "command.repSettings.sub.fuzzy.true", "command.repSettings.sub.fuzzy.false")).queue();
            return;
        }
        var fuzzy = event.getOption("fuzzy").getAsBoolean();

        messageSettings.fuzzyActive(fuzzy);
        if (guildData.updateMessageSettings(event.getGuild(), messageSettings)) {
            event.reply(getBooleanMessage(context, fuzzy,
                    "command.repSettings.sub.fuzzy.true", "command.repSettings.sub.fuzzy.false")).queue();
        }
    }

    private boolean mention(SlashCommandInteractionEvent event, SlashCommandContext context, GuildSettings guildSettings) {
        var messageSettings = guildSettings.messageSettings();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(context, messageSettings.isAnswerActive(),
                    "command.repSettings.sub.mention.true", "command.repSettings.sub.mention.false")).queue();
            return true;
        }
        var mention = event.getOption("mention").getAsBoolean();

        messageSettings.mentionActive(mention);
        if (guildData.updateMessageSettings(event.getGuild(), messageSettings)) {
            event.reply(getBooleanMessage(context, mention,
                    "command.repSettings.sub.mention.true", "command.repSettings.sub.mention.false")).queue();
        }
        return true;
    }

    private void answer(SlashCommandInteractionEvent event, SlashCommandContext context, GuildSettings guildSettings) {
        var messageSettings = guildSettings.messageSettings();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(context, messageSettings.isAnswerActive(),
                    "command.repSettings.sub.answer.true", "command.repSettings.sub.answer.false")).queue();
            return;
        }
        var answer = event.getOption("answer").getAsBoolean();

        messageSettings.answerActive(answer);
        if (guildData.updateMessageSettings(event.getGuild(), messageSettings)) {
            event.reply(getBooleanMessage(context, answer,
                    "command.repSettings.sub.answer.true", "command.repSettings.sub.answer.false")).queue();
        }
    }

    private void embed(SlashCommandInteractionEvent event, SlashCommandContext context, GuildSettings guildSettings) {
        var messageSettings = guildSettings.messageSettings();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(context, messageSettings.isAnswerActive(),
                    "command.repSettings.sub.embed.true", "command.repSettings.sub.embed.false")).queue();
            return;
        }
        var embed = event.getOption("embed").getAsBoolean();

        messageSettings.embedActive(embed);
        if (guildData.updateMessageSettings(event.getGuild(), messageSettings)) {
            event.reply(getBooleanMessage(context, embed,
                    "command.repSettings.sub.embed.true", "command.repSettings.sub.embed.false")).queue();
        }
    }

    private void reactions(SlashCommandInteractionEvent event, SlashCommandContext context, GuildSettings guildSettings) {
        var messageSettings = guildSettings.messageSettings();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(context, messageSettings.isReactionActive(),
                    "command.repSettings.sub.reactions.true", "command.repSettings.sub.reactions.false")).queue();
            return;
        }
        var reactions = event.getOption("reactions").getAsBoolean();

        messageSettings.reactionActive(reactions);
        if (guildData.updateMessageSettings(event.getGuild(), messageSettings)) {
            event.reply(getBooleanMessage(context, reactions,
                    "command.repSettings.sub.reactions.true", "command.repSettings.sub.reactions.false")).queue();
        }
    }

    private void emojidebug(SlashCommandInteractionEvent event, SlashCommandContext context, GuildSettings guildSettings) {
        var generalSettings = guildSettings.generalSettings();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(context, generalSettings.isEmojiDebug(),
                    "command.repSettings.sub.emojidebug.true", "command.repSettings.sub.emojidebug.false")
                        + "\n" + context.localize(emojiExplanation())).queue();
            return;
        }
        var emojidebug = event.getOption("active").getAsBoolean();

        if (guildData.setEmojiDebug(event.getGuild(), emojidebug)) {
            event.reply(getBooleanMessage(context, emojidebug,
                    "command.repSettings.sub.emojidebug.true", "command.repSettings.sub.emojidebug.false")).queue();
        }
    }

    private MessageEmbed getSettings(SlashCommandContext context, GuildSettings guildSettings) {
        var messageSettings = guildSettings.messageSettings();

        return new LocalizedEmbedBuilder(context.localizer())
                .setTitle("command.repSettings.embed.title")
                .appendDescription(messageSettings.toLocalizedString(guildSettings))
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
                emojiString(EmojiDebug.PROMPTED, "command.repSettings.sub.emojidebug.explain.prompted")
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
