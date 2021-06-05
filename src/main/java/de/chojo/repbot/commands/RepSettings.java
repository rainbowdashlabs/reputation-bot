package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.sql.DataSource;
import java.awt.Color;
import java.util.regex.Pattern;

public class RepSettings extends SimpleCommand {
    private final GuildData data;
    private final Localizer loc;
    Pattern emotePattern = Pattern.compile("<:.*?:(?<id>[0-9]*?)>");

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
    public void onSlashCommand(SlashCommandEvent event) {
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
        event.reply(wrap(getSettings(event.getGuild(), guildSettings))).queue();
        return;
    }

    private MessageEmbed getSettings(Guild guild, GuildSettings guildSettings) {
        return new LocalizedEmbedBuilder(loc, guild)
                .setTitle("command.repSettings.embed.title")
                .appendDescription(loc.localize("command.repSettings.embed.descr", guild,
                        Replacement.create("MAX_AGE", guildSettings.getMaxMessageAge()),
                        Replacement.create("REACTION", guildSettings.getReactionMention(guild)),
                        Replacement.create("REACTION_ACTIVE", guildSettings.isReactionActive()),
                        Replacement.create("ANSWER_ACTIVE", guildSettings.isAnswerActive()),
                        Replacement.create("MENTION_ACTIVE", guildSettings.isMentionActive()),
                        Replacement.create("FUZZY_ACTIVE", guildSettings.isFuzzyActive()),
                        Replacement.create("COOLDOWN", guildSettings.getCooldown())
                ))
                .setColor(Color.GREEN)
                .build();
    }

    private boolean cooldown(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.cooldown.get",
                    Replacement.create("MINUTES", guildSettings.getCooldown()))).queue();
            return true;
        }
        var optCooldown = context.argInt(0);

        if (optCooldown.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.notANumber",
                    Replacement.create("INPUT", context.argString(0).get())), 30);
            return false;
        }

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, null, null, null, null, optCooldown.get())) {
            eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.cooldown.set",
                    Replacement.create("MINUTES", optCooldown.get()))).queue();
        }
        return true;
    }

    private void cooldown(SlashCommandEvent event, GuildSettings guildSettings) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        if (event.getOptions().isEmpty()) {
            event.reply(loc.localize("command.repSettings.sub.cooldown.get",
                    Replacement.create("MINUTES", guildSettings.getCooldown()))).queue();
            return;
        }
        var cooldown = event.getOption("minutes").getAsLong();

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, null, null, null, null, (int) cooldown)) {
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

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, null, null, null, optFuzzy.get(), null)) {
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

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, null, null, null, fuzzy, null)) {
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

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, null, null, optMention.get(), null, null)) {
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

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, null, null, mention, null, null)) {
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

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, null, optAnswer.get(), null, null, null)) {
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

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, null, answer, null, null, null)) {
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

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, optReactions.get(), null, null, null, null)) {
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

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, reactions, null, null, null, null)) {
            event.reply(getBooleanMessage(event.getGuild(), reactions,
                    "command.repSettings.sub.reactions.true", "command.repSettings.sub.reactions.false")).queue();
        }
    }

    private boolean reaction(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            if (guildSettings.reactionIsEmote()) {
                eventWrapper.getGuild().retrieveEmoteById(guildSettings.getReaction()).queue(
                        e -> eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.reaction.get.emote",
                                Replacement.create("EMOTE", e.getAsMention()))).queue(),
                        err -> eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.reaction.get.error")).queue());
                return true;
            }
            eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.reaction.get.emoji",
                    Replacement.create("EMOJI", guildSettings.getReaction()))).queue();
            return true;
        }

        String emote = context.argString(0).get();
        var matcher = emotePattern.matcher(emote);
        if (!matcher.find()) {
            eventWrapper.reply("Checking Emote").queue(origM -> {
                origM.addReaction(emote).queue(succ -> {
                    if (data.updateMessageSettings(guildSettings.getGuild(), null, emote, null, null, null, null, null)) {
                        origM.editMessage(loc.localize("command.repSettings.sub.reaction.set.emoji",
                                Replacement.create("EMOJI", emote))).queue();
                    }
                }, err -> origM.editMessage(loc.localize("command.repSettings.error.emojiNotFound")).queue());
            });
            return true;
        }
        var id = matcher.group("id");
        var emoteById = eventWrapper.getGuild().getEmoteById(id);
        if (emoteById == null) {
            eventWrapper.reply(eventWrapper.localize("command.repSettings.error.emojiNotFound")).queue();
            return true;
        }

        if (data.updateMessageSettings(guildSettings.getGuild(), null, id, null, null, null, null, null)) {
            eventWrapper.reply(loc.localize("command.repSettings.sub.reaction.set.emote",
                    Replacement.create("EMOTE", emoteById.getAsMention()))).queue();
        }
        return true;
    }

    private void reaction(SlashCommandEvent event, GuildSettings guildSettings) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        if (event.getOptions().isEmpty()) {
            if (guildSettings.reactionIsEmote()) {
                event.getGuild().retrieveEmoteById(guildSettings.getReaction()).queue(
                        e -> event.reply(loc.localize("command.repSettings.sub.reaction.get.emote",
                                Replacement.create("EMOTE", e.getAsMention()))).queue(),
                        err -> event.reply(loc.localize("command.repSettings.sub.reaction.get.error")).queue());
                return;
            }
            event.reply(loc.localize("command.repSettings.sub.reaction.get.emoji",
                    Replacement.create("EMOJI", guildSettings.getReaction()))).queue();
            return;
        }

        var emote = event.getOption("emote").getAsString();

        var matcher = emotePattern.matcher(emote);
        if (!matcher.find()) {
            event.reply("Checking Emote").queue(message -> {
                message.retrieveOriginal().queue(origM -> {
                    origM.addReaction(emote).queue(succ -> {
                        if (data.updateMessageSettings(guildSettings.getGuild(), null, emote, null, null, null, null, null)) {
                            origM.editMessage(loc.localize("command.repSettings.sub.reaction.set.emoji",
                                    Replacement.create("EMOJI", emote))).queue();
                        }
                    }, err -> origM.editMessage(loc.localize("command.repSettings.error.emojiNotFound")).queue());
                });
            });
            return;
        }
        var id = matcher.group("id");
        var emoteById = event.getGuild().getEmoteById(id);
        if (emoteById == null) {
            event.reply("command.repSettings.error.emojiNotFound").setEphemeral(true).queue();
            return;
        }

        if (data.updateMessageSettings(guildSettings.getGuild(), null, id, null, null, null, null, null)) {
            event.reply(loc.localize("command.repSettings.sub.reaction.set.emote",
                    Replacement.create("EMOTE", emoteById.getAsMention()))).queue(m -> m.retrieveOriginal().queue(m2 -> m2.addReaction(emoteById).queue()));
        }
    }

    private boolean maxMessageAge(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.maxMessageAge.get",
                    Replacement.create("MINUTES", guildSettings.getMaxMessageAge()))).queue();
            return true;
        }
        var optAge = context.argInt(0);

        if (optAge.isEmpty()) {
            eventWrapper.replyErrorAndDelete(context.argString(0).get() + " is not a number", 30);
            return true;
        }
        Integer integer = Math.max(0, optAge.get());
        if (data.updateMessageSettings(guildSettings.getGuild(), integer, null, null, null, null, null, null)) {
            eventWrapper.reply(eventWrapper.localize("command.repSettings.sub.maxMessageAge.get",
                    Replacement.create("MINUTES", integer))).queue();
        }
        return true;
    }

    private void maxMessageAge(SlashCommandEvent event, GuildSettings guildSettings) {
        var loc = this.loc.getContextLocalizer(event.getGuild());
        if (event.getOptions().isEmpty()) {
            event.reply(loc.localize("command.repSettings.sub.maxMessageAge.get",
                    Replacement.create("MINUTES", guildSettings.getMaxMessageAge()))).queue();
            return;
        }
        var age = event.getOption("minutes").getAsLong();

        age = Math.max(0, age);
        if (data.updateMessageSettings(guildSettings.getGuild(), (int) age, null, null, null, null, null, null)) {
            event.reply(loc.localize("command.repSettings.sub.maxMessageAge.get",
                    Replacement.create("MINUTES", age))).queue();
        }
    }

    private String getBooleanMessage(Guild guild, boolean value, String whenTrue, String whenFalse) {
        return loc.localize(value ? whenTrue : whenFalse, guild);
    }
}
