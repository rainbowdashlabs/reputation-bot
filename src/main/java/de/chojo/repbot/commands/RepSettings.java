package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.EmojiDebug;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.jetbrains.annotations.PropertyKey;

import java.awt.Color;
import java.util.List;

//TODO: Replace with select menu to manage states
public class RepSettings extends SimpleCommand {
    private final Guilds guilds;

    public RepSettings(Guilds guilds) {
        super(CommandMeta.builder("repsettings", "command.repSettings.description")
                .addSubCommand("info", "command.repSettings.sub.info")
                .addSubCommand("reactions", "command.repSettings.sub.reactions", argsBuilder()
                        .add(SimpleArgument.bool("reactions", "command.repSettings.sub.reactions.arg.reactions")))
                .addSubCommand("answer", "command.repSettings.sub.answer", argsBuilder()
                        .add(SimpleArgument.bool("answer", "command.repSettings.sub.answer.arg.answer")))
                .addSubCommand("mention", "command.repSettings.sub.mention", argsBuilder()
                        .add(SimpleArgument.bool("mention", "command.repSettings.sub.mention.arg.mention")))
                .addSubCommand("fuzzy", "command.repSettings.sub.fuzzy", argsBuilder()
                        .add(SimpleArgument.bool("fuzzy", "command.repSettings.sub.fuzzy.arg.fuzzy")))
                .addSubCommand("embed", "command.repSettings.sub.embed", argsBuilder()
                        .add(SimpleArgument.bool("embed", "command.repSettings.sub.embed.arg.embed")))
                .addSubCommand("emojidebug", "command.repSettings.sub.emojidebug", argsBuilder()
                        .add(SimpleArgument.bool("active", "command.repSettings.sub.emojidebug.arg.active")))
                .withPermission());
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var guildSettings = guilds.guild(event.getGuild()).settings();

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

    private void sendSettings(SlashCommandInteractionEvent event, SlashCommandContext context, Settings guildSettings) {
        var reactions = SelectMenu.create("reactions")
                .setPlaceholder("Set if reactions can give reputation")
                .setRequiredRange(1, 1)
                .addOption("enabled", "enabled")
                .addOption("disabled", "disabled")
                .build();
        var answers = SelectMenu.create("answers")
                .setPlaceholder("Set if answers can give reputation")
                .setRequiredRange(1, 1)
                .addOption("enabled", "enabled")
                .addOption("disabled", "disabled")
                .build();
        var mention = SelectMenu.create("mention")
                .setPlaceholder("Set if mention can give reputation")
                .setRequiredRange(1, 1)
                .addOption("enabled", "enabled")
                .addOption("disabled", "disabled")
                .build();
        var fuzzy = SelectMenu.create("fuzzy")
                .setPlaceholder("Set if fuzzy matches can give reputation")
                .setRequiredRange(1, 1)
                .addOption("enabled", "enabled")
                .addOption("disabled", "disabled")
                .build();
        var embed = SelectMenu.create("embed")
                .setPlaceholder("Set if embeds should be send to request targets")
                .setRequiredRange(1, 1)
                .addOption("enabled", "enabled", "enable this shit")
                .addOption("disabled", "disabled")
                .build();

        var emojidebug = SelectMenu.create("embed")
                .setPlaceholder("Set if explanatory emojis should be added to messages.")
                .setRequiredRange(1, 1)
                .addOption("enabled", "enabled", "enable this shit")
                .addOption("disabled", "disabled")
                .build();

        event.replyEmbeds(getSettings(context, guildSettings))
                .addActionRows(ActionRow.of(reactions))
                .addActionRows(ActionRow.of(answers))
                .addActionRows(ActionRow.of(mention))
                .addActionRows(ActionRow.of(fuzzy))
                .addActionRows(ActionRow.of(embed))
                //.addActionRows(ActionRow.of(emojidebug))
                .queue();
    }

    private void fuzzy(SlashCommandInteractionEvent event, SlashCommandContext context, Settings guildSettings) {
        var messageSettings = guildSettings.messages();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(context, messageSettings.isFuzzyActive(),
                    "command.repSettings.sub.fuzzy.true", "command.repSettings.sub.fuzzy.false")).queue();
            return;
        }
        var fuzzy = event.getOption("fuzzy").getAsBoolean();

        event.reply(getBooleanMessage(context, messageSettings.fuzzyActive(fuzzy),
                "command.repSettings.sub.fuzzy.true", "command.repSettings.sub.fuzzy.false")).queue();
    }

    private void mention(SlashCommandInteractionEvent event, SlashCommandContext context, Settings guildSettings) {
        var messageSettings = guildSettings.messages();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(context, messageSettings.isAnswerActive(),
                    "command.repSettings.sub.mention.true", "command.repSettings.sub.mention.false")).queue();
            return;
        }
        var mention = event.getOption("mention").getAsBoolean();

        event.reply(getBooleanMessage(context, messageSettings.mentionActive(mention),
                "command.repSettings.sub.mention.true", "command.repSettings.sub.mention.false")).queue();
    }

    private void answer(SlashCommandInteractionEvent event, SlashCommandContext context, Settings guildSettings) {
        var messageSettings = guildSettings.messages();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(context, messageSettings.isAnswerActive(),
                    "command.repSettings.sub.answer.true", "command.repSettings.sub.answer.false")).queue();
            return;
        }
        var answer = event.getOption("answer").getAsBoolean();

        event.reply(getBooleanMessage(context, messageSettings.answerActive(answer),
                "command.repSettings.sub.answer.true", "command.repSettings.sub.answer.false")).queue();
    }

    private void embed(SlashCommandInteractionEvent event, SlashCommandContext context, Settings guildSettings) {
        var messageSettings = guildSettings.messages();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(context, messageSettings.isAnswerActive(),
                    "command.repSettings.sub.embed.true", "command.repSettings.sub.embed.false")).queue();
            return;
        }
        var embed = event.getOption("embed").getAsBoolean();

        event.reply(getBooleanMessage(context, messageSettings.embedActive(embed),
                "command.repSettings.sub.embed.true", "command.repSettings.sub.embed.false")).queue();
    }

    private void reactions(SlashCommandInteractionEvent event, SlashCommandContext context, Settings guildSettings) {
        var messageSettings = guildSettings.messages();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(context, messageSettings.isReactionActive(),
                    "command.repSettings.sub.reactions.true", "command.repSettings.sub.reactions.false")).queue();
            return;
        }
        var reactions = event.getOption("reactions").getAsBoolean();

        event.reply(getBooleanMessage(context, messageSettings.reactionActive(reactions),
                "command.repSettings.sub.reactions.true", "command.repSettings.sub.reactions.false")).queue();
    }

    private void emojidebug(SlashCommandInteractionEvent event, SlashCommandContext context, Settings guildSettings) {
        var generalSettings = guildSettings.general();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(context, generalSettings.isEmojiDebug(),
                    "command.repSettings.sub.emojidebug.true", "command.repSettings.sub.emojidebug.false")
                        + "\n" + context.localize(emojiExplanation())).queue();
            return;
        }
        var emojidebug = event.getOption("active").getAsBoolean();

        event.reply(getBooleanMessage(context, generalSettings.emojiDebug(emojidebug),
                "command.repSettings.sub.emojidebug.true", "command.repSettings.sub.emojidebug.false")).queue();
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
