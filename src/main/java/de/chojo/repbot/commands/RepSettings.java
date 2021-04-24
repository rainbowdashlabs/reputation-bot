package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import emoji4j.EmojiUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import javax.sql.DataSource;
import java.awt.Color;

public class RepSettings extends SimpleCommand {

    private final GuildData data;

    public RepSettings(DataSource source, Localizer localizer) {
        super("repSettings",
                new String[] {"rs"},
                "Get the current reputation settings.",
                "",
                subCommandBuilder()
                        .add("maxMessageAge", "[minutes]", "Get or set the max message age.")
                        .add("reaction", "[emote|emoji]", "Get or set the reputation emote or emoji.")
                        .add("reactions", "[true|false]", "Get or set whether reputation reactions should give reputation.")
                        .add("answer", "[true|false]", "Get or set whether inline replies should be analyzed.")
                        .add("mention", "[true|false]", "Get or set whether mention messages should be analyzed.")
                        .add("fuzzy", "[true|false]", "Get or set whether messages should be searched fuzzy.")
                        .add("cooldown", "[minutes]", "Get or set the reputation cooldown.")
                        .build(),
                Permission.ADMINISTRATOR);
        data = new GuildData(source);
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
            eventWrapper.replyNonMention("You can send reputation to a user every " + guildSettings.getCooldown() + " minutes.").queue();
        }
        var optCooldown = context.argInt(0);

        if (optCooldown.isEmpty()) {
            eventWrapper.replyErrorAndDelete(context.argString(0) + " is not a number", 30);
            return false;
        }

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, null, null, null, null, optCooldown.get())) {
            eventWrapper.replyNonMention("Cooldown set to " + optCooldown.get() + " minutes.").queue();
        }
        return true;
    }

    private boolean fuzzy(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            var fuzzyActive = guildSettings.isFuzzyActive();
            if (fuzzyActive) {
                eventWrapper.replyNonMention("A fuzzy search will be performed when a thankword is detected.").queue();
            } else {
                eventWrapper.replyNonMention("No fuzzy search will be performed when a thankword is detected.").queue();
            }
            return true;
        }
        var optFuzzy = context.argBoolean(0);

        if (optFuzzy.isEmpty()) {
            eventWrapper.replyErrorAndDelete(context.argString(0) + " is not a number", 30);
            return false;
        }

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, null, null, null, optFuzzy.get(), null)) {
            if (optFuzzy.get()) {
                eventWrapper.replyNonMention("A fuzzy search will be performed when a thankword is detected.").queue();
            } else {
                eventWrapper.replyNonMention("No fuzzy search will be performed when a thankword is detected.").queue();
            }
        }
        return true;
    }

    private boolean mention(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            var mentionActive = guildSettings.isMentionActive();
            if (mentionActive) {
                eventWrapper.replyNonMention("Bot will search in mentions.").queue();
            } else {
                eventWrapper.replyNonMention("Bot will not search in mentions.").queue();
            }
            return true;
        }
        var optMention = context.argBoolean(0);

        if (optMention.isEmpty()) {
            eventWrapper.replyErrorAndDelete(context.argString(0) + " is not a number", 30);
            return false;
        }

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, null, null, optMention.get(), null, null)) {
            if (optMention.get()) {
                eventWrapper.replyNonMention("Bot will search in mentions.").queue();
            } else {
                eventWrapper.replyNonMention("Bot will not search in mentions.").queue();
            }
        }
        return true;
    }

    private boolean answer(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            var answerActive = guildSettings.isAnswerActive();
            if (answerActive) {
                eventWrapper.replyNonMention("Bot will search in answers.").queue();
            } else {
                eventWrapper.replyNonMention("Bot will not search in answers.").queue();
            }
            return true;
        }
        var optAnswer = context.argBoolean(0);

        if (optAnswer.isEmpty()) {
            eventWrapper.replyErrorAndDelete(context.argString(0) + " is not a number", 30);
            return false;
        }

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, null, optAnswer.get(), null, null, null)) {
            if (optAnswer.get()) {
                eventWrapper.replyNonMention("Bot will react to mentions.").queue();
            } else {
                eventWrapper.replyNonMention("Bot will not react to mentions.").queue();
            }
        }
        return true;
    }

    private boolean reactions(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            var reactionsActive = guildSettings.isReactionActive();
            if (reactionsActive) {
                eventWrapper.replyNonMention("Bot will process reactions.").queue();
            } else {
                eventWrapper.replyNonMention("Bot will not process reactions.").queue();
            }
            return true;
        }
        var optReactions = context.argBoolean(0);

        if (optReactions.isEmpty()) {
            eventWrapper.replyErrorAndDelete(context.argString(0) + " is not a number", 30);
            return false;
        }

        if (data.updateMessageSettings(guildSettings.getGuild(), null, null, optReactions.get(), null, null, null, null)) {
            if (optReactions.get()) {
                eventWrapper.replyNonMention("Bot will process reactions.").queue();
            } else {
                eventWrapper.replyNonMention("Bot will not process reactions.").queue();
            }
        }
        return true;
    }

    private boolean reaction(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            if (guildSettings.reactionIsEmote()) {
                eventWrapper.getGuild().retrieveEmoteById(guildSettings.getReaction()).queue(
                        e -> eventWrapper.replyNonMention("Current reputation Emote is set to: " + e.getAsMention()).queue(),
                        err -> eventWrapper.replyNonMention("Could not find emote on guild.").queue());
                return true;
            }
            eventWrapper.replyNonMention("Current reputation Emoji is set to " + guildSettings.getReaction()).queue();
            return true;
        }

        var emotes = eventWrapper.getMessage().getEmotes();
        if (emotes.isEmpty()) {
            var emoji = context.argString(0).get();
            if (!EmojiUtils.isEmoji(emoji)) {
                eventWrapper.replyErrorAndDelete("No emoji or emote found.", 10);
                return true;
            }
            if (data.updateMessageSettings(guildSettings.getGuild(), null, emoji, null, null, null, null, null)) {
                eventWrapper.replyNonMention("Reputation Emoji set to " + emoji).queue();
            }
        } else {
            if (emotes.size() > 1) {
                eventWrapper.replyErrorAndDelete("Please enter only one Emote.", 10);
                return true;
            }
            var emote = emotes.get(0);

            if (!Verifier.equalSnowflake(eventWrapper.getGuild(), emote.getGuild())) {
                eventWrapper.replyErrorAndDelete("The emote must be from this server.", 10);
                return true;
            }
            if (data.updateMessageSettings(guildSettings.getGuild(), null, emote.getId(), null, null, null, null, null)) {
                eventWrapper.replyNonMention("Reputation Emote set to " + emote.getAsMention()).queue();
            }
        }
        return true;
    }

    private boolean maxMessageAge(MessageEventWrapper eventWrapper, CommandContext context, GuildSettings guildSettings) {
        if (context.argsEmpty()) {
            eventWrapper.replyNonMention("Messages older than " + guildSettings.getCooldown() + " minutes will be ignored for reactions and answers.").queue();
        }
        var optAge = context.argInt(0);

        if (optAge.isEmpty()) {
            eventWrapper.replyErrorAndDelete(context.argString(0) + " is not a number", 30);
            return false;
        }

        if (data.updateMessageSettings(guildSettings.getGuild(), optAge.get(), null, null, null, null, null, null)) {
            eventWrapper.replyNonMention("Messages older than " + optAge.get() + " minutes will be ignored for reactions and answers.").queue();
        }
        return true;
    }

    private boolean sendSettings(MessageEventWrapper eventWrapper, GuildSettings guildSettings) {
        var settings = new EmbedBuilder()
                .setTitle("Settings")
                .appendDescription(
                        "Max Message Age: " + guildSettings.getMaxMessageAge() + "\n"
                                + "Reputation Reaction: " + guildSettings.getReaction() + "\n"
                                + "Reputation by Reaction: " + guildSettings.isReactionActive() + "\n"
                                + "Reputation by Answer: " + guildSettings.isAnswerActive() + "\n"
                                + "Reputation by Mention: " + guildSettings.isMentionActive() + "\n"
                                + "Reputation by Fuzzy Search: " + guildSettings.isFuzzyActive() + "\n"
                                + "Reputation cooldown: " + guildSettings.getCooldown() + "\n"
                )
                .setColor(Color.GREEN)
                .build();
        eventWrapper.replyNonMention(settings).queue();
        return true;
    }
}
