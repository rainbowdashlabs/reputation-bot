package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import emoji4j.EmojiUtils;
import net.dv8tion.jda.api.Permission;

import javax.sql.DataSource;
import java.awt.Color;

public class RepSettings extends SimpleCommand {

    private final GuildData data;
    private final Localizer loc;

    public RepSettings(DataSource source, Localizer localizer) {
        super("repSettings",
                new String[] {"rs"},
                "command.repSettings.description",
                "",
                subCommandBuilder()
                        .add("maxMessageAge", "[minutes]", "command.repSettings.sub.maxMessageAge")
                        .add("reaction", "[emote|emoji]", "command.repSettings.sub.reaction")
                        .add("reactions", "[true|false]", "command.repSettings.sub.reactions")
                        .add("answer", "[true|false]", "command.repSettings.sub.answer")
                        .add("mention", "[true|false]", "command.repSettings.sub.mention")
                        .add("fuzzy", "[true|false]", "command.repSettings.sub.fuzzy")
                        .add("cooldown", "[minutes]", "command.repSettings.sub.cooldown")
                        .build(),
                Permission.ADMINISTRATOR);
        data = new GuildData(source);
        loc = localizer;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        var optGuildSettings = data.getGuildSettings(eventWrapper.getGuild());
        if ((optGuildSettings.isEmpty())) return false;

        var guildSettings = optGuildSettings.get();
        if (context.argsEmpty()) {
            return sendSettings(eventWrapper, guildSettings);
        }

        var subcmd = context.argString(0).get();

        if ("maxMessageAge".equalsIgnoreCase(subcmd)) {
            return maxMessageAge(eventWrapper, context.subCommandcontext(subcmd), guildSettings);
        }

        if ("reaction".equalsIgnoreCase(subcmd)) {
            return reaction(eventWrapper, context.subCommandcontext(subcmd), guildSettings);

        }

        if ("reactions".equalsIgnoreCase(subcmd)) {
            return reactions(eventWrapper, context.subCommandcontext(subcmd), guildSettings);

        }

        if ("answer".equalsIgnoreCase(subcmd)) {
            return answer(eventWrapper, context.subCommandcontext(subcmd), guildSettings);

        }

        if ("mention".equalsIgnoreCase(subcmd)) {
            return mention(eventWrapper, context.subCommandcontext(subcmd), guildSettings);

        }

        if ("fuzzy".equalsIgnoreCase(subcmd)) {
            return fuzzy(eventWrapper, context.subCommandcontext(subcmd), guildSettings);

        }

        if ("cooldown".equalsIgnoreCase(subcmd)) {
            return cooldown(eventWrapper, context.subCommandcontext(subcmd), guildSettings);

        }
        return true;
    }

    private boolean cooldown(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.cooldown.get", eventWrapper,
                    Replacement.create("MINUTES", guildSettings.getCooldown()))).queue();
        }
        var optCooldown = context.argInt(0);

        if (optCooldown.isEmpty()) {
            eventWrapper.replyErrorAndDelete(loc.localize("error.notANumber", eventWrapper,
                    Replacement.create("INPUT", context.argString(0))), 30);
            return false;
        }

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, null, null, null, null, optCooldown.get())) {
            eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.cooldown.set", eventWrapper,
                    Replacement.create("MINUTES", optCooldown.get()))).queue();
        }
        return true;
    }

    private boolean fuzzy(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            var fuzzyActive = guildSettings.isFuzzyActive();
            if (fuzzyActive) {
                eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.fuzzy.true", eventWrapper)).queue();
            } else {
                eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.fuzzy.false", eventWrapper)).queue();
            }
            return true;
        }
        var optFuzzy = context.argBoolean(0);

        if (optFuzzy.isEmpty()) {
            eventWrapper.replyErrorAndDelete(loc.localize("error.notABoolean", eventWrapper,
                    Replacement.create("INPUT", context.argString(0))), 30);
            return false;
        }

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, null, null, null, optFuzzy.get(), null)) {
            if (optFuzzy.get()) {
                eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.fuzzy.true", eventWrapper)).queue();
            } else {
                eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.fuzzy.false", eventWrapper)).queue();
            }
        }
        return true;
    }

    private boolean mention(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            var mentionActive = guildSettings.isMentionActive();
            if (mentionActive) {
                eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.mention.true", eventWrapper)).queue();
            } else {
                eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.mention.false", eventWrapper)).queue();
            }
            return true;
        }
        var optMention = context.argBoolean(0);

        if (optMention.isEmpty()) {
            eventWrapper.replyErrorAndDelete(loc.localize("error.notABoolean", eventWrapper,
                    Replacement.create("INPUT", context.argString(0))), 30);
            return false;
        }

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, null, null, optMention.get(), null, null)) {
            if (optMention.get()) {
                eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.mention.true", eventWrapper)).queue();
            } else {
                eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.mention.false", eventWrapper)).queue();
            }
        }
        return true;
    }

    private boolean answer(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            var answerActive = guildSettings.isAnswerActive();
            if (answerActive) {
                eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.answer.true", eventWrapper)).queue();
            } else {
                eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.answer.false", eventWrapper)).queue();
            }
            return true;
        }
        var optAnswer = context.argBoolean(0);

        if (optAnswer.isEmpty()) {
            eventWrapper.replyErrorAndDelete(loc.localize("error.notABoolean", eventWrapper,
                    Replacement.create("INPUT", context.argString(0))), 30);
            return false;
        }

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, null, optAnswer.get(), null, null, null)) {
            if (optAnswer.get()) {
                eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.answer.true", eventWrapper)).queue();
            } else {
                eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.answer.false", eventWrapper)).queue();
            }
        }
        return true;
    }

    private boolean reactions(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            var reactionsActive = guildSettings.isReactionActive();
            if (reactionsActive) {
                eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.reactions.true", eventWrapper)).queue();
            } else {
                eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.reactions.false", eventWrapper)).queue();
            }
            return true;
        }
        var optReactions = context.argBoolean(0);

        if (optReactions.isEmpty()) {
            eventWrapper.replyErrorAndDelete(loc.localize("error.notABoolean", eventWrapper,
                    Replacement.create("INPUT", context.argString(0))), 30);
            return false;
        }

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, optReactions.get(), null, null, null, null)) {
            if (optReactions.get()) {
                eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.reactions.true", eventWrapper)).queue();
            } else {
                eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.reactions.false", eventWrapper)).queue();
            }
        }
        return true;
    }

    private boolean reaction(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            if (guildSettings.reactionIsEmote()) {
                eventWrapper.getGuild().retrieveEmoteById(guildSettings.getReaction()).queue(
                        e -> eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.reaction.get.emote", eventWrapper,
                                Replacement.create("EMOTE", e.getAsMention()))).queue(),
                        err -> eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.reaction.get.error", eventWrapper)).queue());
                return true;
            }
            eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.reaction.get.emoji", eventWrapper,
                    Replacement.create("EMOJI", guildSettings.getReaction()))).queue();
            return true;
        }

        var emotes = eventWrapper.getMessage().getEmotes();
        if (emotes.isEmpty()) {
            var emoji = context.argString(0).get();
            if (!EmojiUtils.isEmoji(emoji)) {
                eventWrapper.replyErrorAndDelete(loc.localize("command.repSettings.error.emojiNotFound", eventWrapper), 10);
                return true;
            }
            if (data.updateMessageSettings(guildSettings.getGuild(), null, emoji, null, null, null, null, null)) {
                eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.reaction.set.emoji", eventWrapper,
                        Replacement.create("EMOJI", emoji))).queue();
            }
        } else {
            if (emotes.size() > 1) {
                eventWrapper.replyErrorAndDelete(loc.localize("command.repSettings.error.multi", eventWrapper), 10);
                return true;
            }
            var emote = emotes.get(0);

            if (!Verifier.equalSnowflake(eventWrapper.getGuild(), emote.getGuild())) {
                eventWrapper.replyErrorAndDelete(loc.localize("command.repSettings.error.otherServer", eventWrapper), 10);
                return true;
            }
            if (data.updateMessageSettings(guildSettings.getGuild(), null, emote.getId(), null, null, null, null, null)) {
                eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.reaction.set.emote", eventWrapper,
                        Replacement.create("EMOTE", emote.getAsMention()))).queue();
            }
        }
        return true;
    }

    private boolean maxMessageAge(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.maxMessageAge.get", eventWrapper,
                    Replacement.create("MINUTES", guildSettings.getCooldown()))).queue();
        }
        var optAge = context.argInt(0);

        if (optAge.isEmpty()) {
            eventWrapper.replyErrorAndDelete(context.argString(0) + " is not a number", 30);
            return false;
        }

        if (data.updateMessageSettings(guildSettings.getGuild(), optAge.get(), null, null, null, null, null, null)) {
            eventWrapper.replyNonMention(loc.localize("command.repSettings.sub.maxMessageAge.get", eventWrapper,
                    Replacement.create("MINUTES", optAge.get()))).queue();
        }
        return true;
    }

    private boolean sendSettings(MessageEventWrapper eventWrapper, GuildSettings guildSettings) {
        var settings = new LocalizedEmbedBuilder(loc, eventWrapper)
                .setTitle("command.repSettings.embed.title")
                .appendDescription(loc.localize("command.repSettings.embed.descr", eventWrapper,
                        Replacement.create("MAX_AGE", guildSettings.getMaxMessageAge()),
                        Replacement.create("REACTION", guildSettings.getReactionMention(eventWrapper.getGuild())),
                        Replacement.create("REACTION_ACTIVE", guildSettings.isReactionActive()),
                        Replacement.create("ANSWER_ACTIVE", guildSettings.isAnswerActive()),
                        Replacement.create("MENTION_ACTIVE", guildSettings.isMentionActive()),
                        Replacement.create("FUZZY_ACTIVE", guildSettings.isFuzzyActive()),
                        Replacement.create("COOLDOWN", guildSettings.getCooldown())
                ))
                .setColor(Color.GREEN)
                .build();
        eventWrapper.replyNonMention(settings).queue();
        return true;
    }
}
