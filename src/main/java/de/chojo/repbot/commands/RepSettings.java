package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GuildSettingUpdate;
import de.chojo.repbot.data.wrapper.GuildSettings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.sql.DataSource;
import java.awt.Color;
import java.util.regex.Pattern;

public class RepSettings extends SimpleCommand {
    private final GuildData data;
    private final Localizer loc;
    private final Pattern emotePattern = Pattern.compile("<:.*?:(?<id>[0-9]*?)>");

    public RepSettings(DataSource source, Localizer localizer) {
        super("repsettings",
                new String[]{"rs"},
                "command.repSettings.description",
                subCommandBuilder()
                        .add("info", "command.repSettings.sub.info", argsBuilder()
                                .build()
                        )
                        .add("maxmessageage", "command.repSettings.sub.maxMessageAge", argsBuilder()
                                .add(OptionType.INTEGER, "minutes", "minutes")
                                .build()
                        )
                        .add("minmessages", "command.repSettings.sub.minMessages", argsBuilder()
                                .add(OptionType.INTEGER, "messages", "messages")
                                .build()
                        )
                        .add("reaction", "command.repSettings.sub.reaction", argsBuilder()
                                .add(OptionType.STRING, "emote", "emote")
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
                        .add("cooldown", "command.repSettings.sub.cooldown", argsBuilder()
                                .add(OptionType.INTEGER, "minutes", "minutes")
                                .build()
                        )
                        .build(),
                Permission.MANAGE_SERVER);
        data = new GuildData(source);
        loc = localizer;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        var optGuildSettings = data.getGuildSettings(eventWrapper.getGuild());
        if ((optGuildSettings.isEmpty())) return false;

        if (context.argsEmpty()) {
            return false;
        }

        var guildSettings = optGuildSettings.get();
        var subcmd = context.argString(0).get();
        if ("info".equalsIgnoreCase(subcmd)) {
            return sendSettings(eventWrapper, guildSettings);
        }

        if ("maxMessageAge".equalsIgnoreCase(subcmd)) {
            return maxMessageAge(eventWrapper, context.subContext(subcmd), guildSettings);
        }

        if ("minMessages".equalsIgnoreCase(subcmd)) {
            return minMessages(eventWrapper, context.subContext(subcmd), guildSettings);
        }

        if ("reaction".equalsIgnoreCase(subcmd)) {
            return reaction(eventWrapper, context.subContext(subcmd), guildSettings);

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

        if ("cooldown".equalsIgnoreCase(subcmd)) {
            return cooldown(eventWrapper, context.subContext(subcmd), guildSettings);

        }
        return false;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        var optGuildSettings = data.getGuildSettings(event.getGuild());
        if ((optGuildSettings.isEmpty())) return;
        var guildSettings = optGuildSettings.get();

        var subcmd = event.getSubcommandName();
        if ("info".equalsIgnoreCase(subcmd)) {
            sendSettings(event, guildSettings);
        }

        if ("maxMessageAge".equalsIgnoreCase(subcmd)) {
            maxMessageAge(event, guildSettings);
        }
        if ("minMessages".equalsIgnoreCase(subcmd)) {
            minMessages(event, guildSettings);
        }

        if ("reaction".equalsIgnoreCase(subcmd)) {
            reaction(event, guildSettings);

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

        if ("cooldown".equalsIgnoreCase(subcmd)) {
            cooldown(event, guildSettings);
        }
    }

    private boolean sendSettings(MessageEventWrapper eventWrapper, GuildSettings guildSettings) {
        eventWrapper.reply(getSettings(eventWrapper.getGuild(), guildSettings)).queue();
        return true;
    }

    private void sendSettings(SlashCommandEvent event, GuildSettings guildSettings) {
        event.replyEmbeds(getSettings(event.getGuild(), guildSettings)).queue();
        return;
    }

    private MessageEmbed getSettings(Guild guild, GuildSettings guildSettings) {
        return new LocalizedEmbedBuilder(loc, guild)
                .setTitle("command.repSettings.embed.title")
                .appendDescription(loc.localize("command.repSettings.embed.descr", guild,
                        Replacement.create("MAX_AGE", guildSettings.maxMessageAge()),
                        Replacement.create("MIN_MESSAGES", guildSettings.minMessages()),
                        Replacement.create("REACTION", guildSettings.reactionMention(guild)),
                        Replacement.create("REACTION_ACTIVE", guildSettings.isReactionActive()),
                        Replacement.create("ANSWER_ACTIVE", guildSettings.isAnswerActive()),
                        Replacement.create("MENTION_ACTIVE", guildSettings.isMentionActive()),
                        Replacement.create("FUZZY_ACTIVE", guildSettings.isFuzzyActive()),
                        Replacement.create("COOLDOWN", guildSettings.cooldown())
                ))
                .setColor(Color.GREEN)
                .build();
    }

    private boolean cooldown(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.cooldown.get",
                    Replacement.create("MINUTES", guildSettings.cooldown()))).queue();
            return true;
        }
        var optCooldown = context.argInt(0);

        if (optCooldown.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.notANumber",
                    Replacement.create("INPUT", context.argString(0).get())), 30);
            return false;
        }

        if (data.updateMessageSettings(GuildSettingUpdate.builder(eventWrapper.getGuild()).cooldown(optCooldown.get()).build())) {
            eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.cooldown.set",
                    Replacement.create("MINUTES", optCooldown.get()))).queue();
        }
        return true;
    }

    private void cooldown(SlashCommandEvent event, GuildSettings guildSettings) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        if (event.getOptions().isEmpty()) {
            event.reply(loc.localize("command.repSettings.sub.cooldown.get",
                    Replacement.create("MINUTES", guildSettings.cooldown()))).queue();
            return;
        }
        var cooldown = event.getOption("minutes").getAsLong();

        if (data.updateMessageSettings(GuildSettingUpdate.builder(event.getGuild()).cooldown((int) cooldown).build())) {
            event.reply(loc.localize("command.repSettings.sub.cooldown.set",
                    Replacement.create("MINUTES", cooldown))).queue();
        }
    }

    private boolean fuzzy(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), guildSettings.isFuzzyActive(),
                    "command.repSettings.sub.fuzzy.true", "command.repSettings.sub.fuzzy.false")).queue();
            return true;
        }
        var optFuzzy = context.argBoolean(0);

        if (optFuzzy.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.notABoolean",
                    Replacement.create("INPUT", context.argString(0))), 30);
            return false;
        }

        if (data.updateMessageSettings(GuildSettingUpdate.builder(eventWrapper.getGuild()).fuzzyActive(optFuzzy.get()).build())) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), optFuzzy.get(),
                    "command.repSettings.sub.fuzzy.true", "command.repSettings.sub.fuzzy.false")).queue();
        }
        return true;
    }

    private void fuzzy(SlashCommandEvent event, GuildSettings guildSettings) {
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(event.getGuild(), guildSettings.isFuzzyActive(),
                    "command.repSettings.sub.fuzzy.true", "command.repSettings.sub.fuzzy.false")).queue();
            return;
        }
        var fuzzy = event.getOption("fuzzy").getAsBoolean();

        if (data.updateMessageSettings(GuildSettingUpdate.builder(event.getGuild()).fuzzyActive(fuzzy).build())) {
            event.reply(getBooleanMessage(event.getGuild(), fuzzy,
                    "command.repSettings.sub.fuzzy.true", "command.repSettings.sub.fuzzy.false")).queue();
        }
    }

    private boolean mention(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), guildSettings.isMentionActive(),
                    "command.repSettings.sub.mention.true", "command.repSettings.sub.mention.false")).queue();
            return true;
        }
        var optMention = context.argBoolean(0);

        if (optMention.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.notABoolean",
                    Replacement.create("INPUT", context.argString(0))), 30);
            return false;
        }

        if (data.updateMessageSettings(GuildSettingUpdate.builder(eventWrapper.getGuild()).mentionActive(optMention.get()).build())) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), optMention.get(),
                    "command.repSettings.sub.mention.true", "command.repSettings.sub.mention.false")).queue();
        }
        return true;
    }

    private boolean mention(SlashCommandEvent event, GuildSettings guildSettings) {
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(event.getGuild(), guildSettings.isMentionActive(),
                    "command.repSettings.sub.mention.true", "command.repSettings.sub.mention.false")).queue();
            return true;
        }
        var mention = event.getOption("mention").getAsBoolean();

        if (data.updateMessageSettings(GuildSettingUpdate.builder(event.getGuild()).mentionActive(mention).build())) {
            event.reply(getBooleanMessage(event.getGuild(), mention,
                    "command.repSettings.sub.mention.true", "command.repSettings.sub.mention.false")).queue();
        }
        return true;
    }

    private boolean answer(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), guildSettings.isAnswerActive(),
                    "command.repSettings.sub.answer.true", "command.repSettings.sub.answer.false")).queue();
            return true;
        }
        var optAnswer = context.argBoolean(0);

        if (optAnswer.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.notABoolean",
                    Replacement.create("INPUT", context.argString(0))), 30);
            return false;
        }

        if (data.updateMessageSettings(GuildSettingUpdate.builder(eventWrapper.getGuild()).answerActive(optAnswer.get()).build())) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), optAnswer.get(),
                    "command.repSettings.sub.answer.true", "command.repSettings.sub.answer.false")).queue();
        }
        return true;
    }

    private void answer(SlashCommandEvent event, GuildSettings guildSettings) {
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(event.getGuild(), guildSettings.isAnswerActive(),
                    "command.repSettings.sub.answer.true", "command.repSettings.sub.answer.false")).queue();
            return;
        }
        var answer = event.getOption("answer").getAsBoolean();

        if (data.updateMessageSettings(GuildSettingUpdate.builder(event.getGuild()).answerActive(answer).build())) {
            event.reply(getBooleanMessage(event.getGuild(), answer,
                    "command.repSettings.sub.answer.true", "command.repSettings.sub.answer.false")).queue();
        }
    }

    private boolean reactions(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), guildSettings.isReactionActive(),
                    "command.repSettings.sub.reactions.true", "command.repSettings.sub.reactions.false")).queue();
            return true;
        }
        var optReactions = context.argBoolean(0);

        if (optReactions.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.notABoolean",
                    Replacement.create("INPUT", context.argString(0))), 30);
            return false;
        }

        if (data.updateMessageSettings(GuildSettingUpdate.builder(eventWrapper.getGuild()).reactionsActive(optReactions.get()).build())) {
            eventWrapper.reply(getBooleanMessage(eventWrapper.getGuild(), optReactions.get(),
                    "command.repSettings.sub.reactions.true", "command.repSettings.sub.reactions.false")).queue();
        }
        return true;
    }

    private void reactions(SlashCommandEvent event, GuildSettings guildSettings) {
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(event.getGuild(), guildSettings.isReactionActive(),
                    "command.repSettings.sub.reactions.true", "command.repSettings.sub.reactions.false")).queue();
            return;
        }
        var reactions = event.getOption("reactions").getAsBoolean();

        if (data.updateMessageSettings(GuildSettingUpdate.builder(event.getGuild()).reactionsActive(reactions).build())) {
            event.reply(getBooleanMessage(event.getGuild(), reactions,
                    "command.repSettings.sub.reactions.true", "command.repSettings.sub.reactions.false")).queue();
        }
    }

    private boolean reaction(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            if (guildSettings.reactionIsEmote()) {
                eventWrapper.getGuild().retrieveEmoteById(guildSettings.reaction())
                        .flatMap(e -> eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.reaction.get.emote",
                                Replacement.create("EMOTE", e.getAsMention()))))
                        .onErrorFlatMap(
                                err -> eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.reaction.get.error"))
                        ).queue();
                return true;
            }
            eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.reaction.get.emoji",
                    Replacement.create("EMOJI", guildSettings.reaction()))).queue();
            return true;
        }

        var emote = context.argString(0).get();
        var matcher = emotePattern.matcher(emote);
        if (!matcher.find()) {
            eventWrapper.reply("Checking Emote").flatMap(origM -> origM.addReaction(emote)
                    .onErrorFlatMap(err -> origM.editMessage("").map(x -> null))
                    .map(succ -> {
                        if (data.updateMessageSettings(GuildSettingUpdate.builder(eventWrapper.getGuild()).reaction(emote).build())) {
                            return origM.editMessage(loc.localize("command.repSettings.sub.reaction.set.emoji",
                                    Replacement.create("EMOJI", emote)));
                        }
                        return null;
                    })).queue();
            return true;
        }
        var id = matcher.group("id");
        var emoteById = eventWrapper.getGuild().getEmoteById(id);
        if (emoteById == null) {
            eventWrapper.reply(eventWrapper.localize("command.repSettings.error.emojiNotFound")).queue();
            return true;
        }

        if (data.updateMessageSettings(GuildSettingUpdate.builder(eventWrapper.getGuild()).reaction(id).build())) {
            eventWrapper.reply(loc.localize("command.repSettings.sub.reaction.set.emote",
                    Replacement.create("EMOTE", emoteById.getAsMention()))).queue();
        }
        return true;
    }

    private void reaction(SlashCommandEvent event, GuildSettings guildSettings) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        if (event.getOptions().isEmpty()) {
            if (guildSettings.reactionIsEmote()) {
                event.getGuild().retrieveEmoteById(guildSettings.reaction())
                        .flatMap(e -> event.reply(
                                loc.localize("command.repSettings.sub.reaction.get.emote",
                                        Replacement.create("EMOTE", e.getAsMention()))))
                        .onErrorFlatMap(
                                err -> event.reply(loc.localize("command.repSettings.sub.reaction.get.error")))
                        .queue();
                return;
            }
            event.reply(loc.localize("command.repSettings.sub.reaction.get.emoji",
                    Replacement.create("EMOJI", guildSettings.reaction()))).queue();
            return;
        }

        var emote = event.getOption("emote").getAsString();

        var matcher = emotePattern.matcher(emote);
        if (!matcher.find()) {
            event.reply("Checking Emote")
                    .flatMap(InteractionHook::retrieveOriginal)
                    .flatMap(origM -> origM.addReaction(emote)
                            .onErrorFlatMap(err -> origM.editMessage(loc.localize("command.repSettings.error.emojiNotFound")).map(x -> null))
                            .map(succ -> {
                                if (data.updateMessageSettings(GuildSettingUpdate.builder(event.getGuild()).reaction(emote).build())) {
                                    origM.editMessage(loc.localize("command.repSettings.sub.reaction.set.emoji",
                                            Replacement.create("EMOJI", emote))).queue();
                                }
                                return null;
                            }))
                    .queue();

            return;
        }
        var id = matcher.group("id");
        var emoteById = event.getGuild().getEmoteById(id);
        if (emoteById == null) {
            event.reply("command.repSettings.error.emojiNotFound").setEphemeral(true).queue();
            return;
        }

        if (data.updateMessageSettings(GuildSettingUpdate.builder(event.getGuild()).reaction(id).build())) {
            event.reply(loc.localize("command.repSettings.sub.reaction.set.emote",
                    Replacement.create("EMOTE", emoteById.getAsMention()))).flatMap(InteractionHook::retrieveOriginal).flatMap(m2 -> m2.addReaction(emoteById)).queue();
        }
    }

    private boolean maxMessageAge(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.maxMessageAge.get",
                    Replacement.create("MINUTES", guildSettings.maxMessageAge()))).queue();
            return true;
        }
        var optAge = context.argInt(0);

        if (optAge.isEmpty()) {
            eventWrapper.replyErrorAndDelete(context.argString(0).get() + " is not a number", 30);
            return true;
        }
        Integer age = Math.max(0, optAge.get());
        if (data.updateMessageSettings(GuildSettingUpdate.builder(eventWrapper.getGuild()).maxMessageAge(age).build())) {
            eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.maxMessageAge.get",
                    Replacement.create("MINUTES", age))).queue();
        }
        return true;
    }

    private void maxMessageAge(SlashCommandEvent event, GuildSettings guildSettings) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        if (event.getOptions().isEmpty()) {
            event.reply(loc.localize("command.repSettings.sub.maxMessageAge.get",
                    Replacement.create("MINUTES", guildSettings.maxMessageAge()))).queue();
            return;
        }
        var age = event.getOption("minutes").getAsLong();

        age = Math.max(0, age);
        if (data.updateMessageSettings(GuildSettingUpdate.builder(event.getGuild()).maxMessageAge((int) age).build())) {
            event.reply(loc.localize("command.repSettings.sub.maxMessageAge.get",
                    Replacement.create("MINUTES", age))).queue();
        }
    }

    private boolean minMessages(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.minMessages.get",
                    Replacement.create("MINUTES", guildSettings.minMessages()))).queue();
            return true;
        }
        var optAge = context.argInt(0);

        if (optAge.isEmpty()) {
            eventWrapper.replyErrorAndDelete(context.argString(0).get() + " is not a number", 30);
            return true;
        }
        Integer minMessages = Math.max(0, Math.min(optAge.get(), 100));
        if (data.updateMessageSettings(GuildSettingUpdate.builder(eventWrapper.getGuild()).minMessages(minMessages).build())) {
            eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.minMessages.get",
                    Replacement.create("AMOUNT", minMessages))).queue();
        }
        return true;
    }

    private void minMessages(SlashCommandEvent event, GuildSettings guildSettings) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        if (event.getOptions().isEmpty()) {
            event.reply(loc.localize("command.repSettings.sub.minMessages.get",
                    Replacement.create("AMOUNT", guildSettings.minMessages()))).queue();
            return;
        }
        var minMessages = event.getOption("messages").getAsLong();

        minMessages = Math.max(0, Math.min(minMessages, 100));
        if (data.updateMessageSettings(GuildSettingUpdate.builder(event.getGuild()).minMessages((int) minMessages).build())) {
            event.reply(loc.localize("command.repSettings.sub.minMessages.get",
                    Replacement.create("AMOUNT", minMessages))).queue();
        }
    }

    private String getBooleanMessage(Guild guild, boolean value, String whenTrue, String whenFalse) {
        return loc.localize(value ? whenTrue : whenFalse, guild);
    }
}
