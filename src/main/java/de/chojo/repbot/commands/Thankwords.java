package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.data.GuildData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class Thankwords extends SimpleCommand {

    private final GuildData data;

    public Thankwords(DataSource dataSource, Localizer localizer) {
        super("thankwords", new String[] {"tw"},
                "Configure Thankwords",
                null,
                subCommandBuilder()
                        .add("add", "<pattern>", "Add another pattern. Pattern are interpreted as [Regex](https://www.regextester.com/). A pattern should not match spaces.")
                        .add("remove", "<pattern>", "Delete a pattern")
                        .add("list", null, "List all pattern")
                        .add("check", "<Sentence>", "Check if a message would give reputation.")
                        .build(),
                Permission.ADMINISTRATOR);
        data = new GuildData(dataSource);
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;
        var subCmd = context.argString(0).get();
        if ("add".equalsIgnoreCase(subCmd)) {
            return add(eventWrapper, context.subCommandcontext(subCmd));
        }
        if ("remove".equalsIgnoreCase(subCmd)) {
            return remove(eventWrapper, context.subCommandcontext(subCmd));
        }
        if ("list".equalsIgnoreCase(subCmd)) {
            return list(eventWrapper);
        }
        if ("check".equalsIgnoreCase(subCmd)) {
            return check(eventWrapper);
        }
        return false;
    }

    private boolean add(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;

        var pattern = context.argString(0).get();
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            eventWrapper.replyErrorAndDelete("Invalid regex pattern.", 30);
            return true;
        }
        if (data.addThankWord(eventWrapper.getGuild(), pattern)) {
            eventWrapper.replyNonMention("Added pattern `" + pattern + "`.").queue();
        }
        return true;
    }

    private boolean remove(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;

        var pattern = context.argString(0).get();
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            eventWrapper.replyErrorAndDelete("Invalid regex pattern.", 30);
            return true;
        }
        if (data.removeThankWord(eventWrapper.getGuild(), pattern)) {
            eventWrapper.replyNonMention("Removed pattern `" + pattern + "`.").queue();
            return true;
        }
        eventWrapper.replyErrorAndDelete("Pattern does not exist.", 10);
        return true;
    }

    private boolean list(MessageEventWrapper eventWrapper) {
        var optGuildSettings = data.getGuildSettings(eventWrapper.getGuild());
        if (optGuildSettings.isEmpty()) return false;

        var guildSettings = optGuildSettings.get();

        var pattern = Arrays.stream(guildSettings.getThankwords())
                .map(w -> StringUtils.wrap(w, "`"))
                .collect(Collectors.joining(", "));

        eventWrapper.replyNonMention("Following Pattern are active:\n" + pattern).queue();
        return true;
    }

    private boolean check(MessageEventWrapper eventWrapper) {
        var optGuildSettings = data.getGuildSettings(eventWrapper.getGuild());
        if (optGuildSettings.isEmpty()) return false;

        var guildSettings = optGuildSettings.get();

        var result = MessageAnalyzer.processMessage(guildSettings.getThankwordPattern(), eventWrapper.getMessage());

        switch (result.getType()) {
            case FUZZY -> {
                var match = new EmbedBuilder()
                        .setTitle("Fuzzy Match")
                        .setDescription("Donator: " + result.getDonator().getAsMention() + "\n"
                                + "Receiver: " + result.getReceiver().getAsMention() + "\n"
                                + "Confidence Score: " + String.format("%.3f", result.getConfidenceScore()));

                if (result.getConfidenceScore() < 0.85) {
                    match.setFooter("Confidence Score is to low. A reputation will not be given.");
                }

                eventWrapper.replyNonMention(match.build()).queue();
            }
            case MENTION -> {
                var match = new EmbedBuilder()
                        .setTitle("Mention Match")
                        .setDescription("Donator: " + result.getDonator().getAsMention() + "\n"
                                + "Receiver: " + result.getReceiver().getAsMention());
                eventWrapper.replyNonMention(match.build()).queue();
            }
            case ANSWER -> {
                var match = new EmbedBuilder()
                        .setTitle("Answer Match")
                        .setDescription(
                                "Donator: " + result.getDonator().getAsMention() + "\n"
                                        + "Receiver: " + result.getReceiver().getAsMention() + "\n"
                                        + "Reference Message: [Jump](" + result.getReferenceMessage().getJumpUrl() + ")");
                eventWrapper.replyNonMention(match.build()).queue();
            }
            case NO_MATCH -> eventWrapper.replyNonMention("No match found.").queue();
        }
        return true;
    }
}
