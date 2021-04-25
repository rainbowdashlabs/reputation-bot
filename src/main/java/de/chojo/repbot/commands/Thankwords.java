package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
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
    private final Localizer loc;

    public Thankwords(DataSource dataSource, Localizer localizer) {
        super("thankwords", new String[] {"tw"},
                "command.thankwords.description",
                null,
                subCommandBuilder()
                        .add("add", "<pattern>", "command.thankwords.sub.add")
                        .add("remove", "<pattern>", "command.thankwords.sub.remove")
                        .add("list", null, "command.thankwords.sub.list")
                        .add("check", "<Sentence>", "command.thankwords.sub.check")
                        .build(),
                Permission.ADMINISTRATOR);
        data = new GuildData(dataSource);
        loc = localizer;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;
        var subCmd = context.argString(0).get();
        if ("add".equalsIgnoreCase(subCmd)) {
            return add(eventWrapper, context.subContext(subCmd));
        }
        if ("remove".equalsIgnoreCase(subCmd)) {
            return remove(eventWrapper, context.subContext(subCmd));
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
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("command.thankwords.error.invalidRegex"), 30);
            return true;
        }
        if (data.addThankWord(eventWrapper.getGuild(), pattern)) {
            eventWrapper.reply(eventWrapper.localize("command.thankwords.sub.add.added",
                    Replacement.create("PATTERN", pattern, Format.CODE))).queue();
        }
        return true;
    }

    private boolean remove(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;

        var pattern = context.argString(0).get();
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("command.thankwords.error.invalidRegex"), 30);
            return true;
        }
        if (data.removeThankWord(eventWrapper.getGuild(), pattern)) {
            eventWrapper.reply(eventWrapper.localize("command.thankwords.sub.remove.removed",
                    Replacement.create("PATTERN", pattern, Format.CODE))).queue();
            return true;
        }
        eventWrapper.replyErrorAndDelete(eventWrapper.localize("command.thankwords.error.patternNotFound"), 10);
        return true;
    }

    private boolean list(MessageEventWrapper eventWrapper) {
        var optGuildSettings = data.getGuildSettings(eventWrapper.getGuild());
        if (optGuildSettings.isEmpty()) return false;

        var guildSettings = optGuildSettings.get();

        var pattern = Arrays.stream(guildSettings.getThankwords())
                .map(w -> StringUtils.wrap(w, "`"))
                .collect(Collectors.joining(", "));

        eventWrapper.reply(eventWrapper.localize("command.thankwords.sub.list.list") + "\n" + pattern).queue();
        return true;
    }

    private boolean check(MessageEventWrapper eventWrapper) {
        var optGuildSettings = data.getGuildSettings(eventWrapper.getGuild());
        if (optGuildSettings.isEmpty()) return false;

        var guildSettings = optGuildSettings.get();

        var result = MessageAnalyzer.processMessage(guildSettings.getThankwordPattern(), eventWrapper.getMessage());

        switch (result.getType()) {
            case FUZZY -> {
                var match = new LocalizedEmbedBuilder(loc, eventWrapper)
                        .setTitle("command.thankwords.sub.check.match.fuzzy")
                        .setDescription(
                                eventWrapper.localize("command.thankwords.sub.check.result",
                                        Replacement.create("DONATOR", result.getDonator().getAsMention()),
                                        Replacement.create("RECEIVER", result.getReceiver().getAsMention())) + "\n"
                                        + eventWrapper.localize("command.thankwords.sub.check.confidence",
                                        Replacement.create("SCORE", String.format("%.3f", result.getConfidenceScore()))));

                if (result.getConfidenceScore() < 0.85) {
                    match.setFooter("command.thankwords.sub.check.notConfident");
                }

                eventWrapper.reply(match.build()).queue();
            }
            case MENTION -> {
                var match = new LocalizedEmbedBuilder(loc, eventWrapper)
                        .setTitle("command.thankwords.sub.check.match.mention")
                        .setDescription(
                                eventWrapper.localize("command.thankwords.sub.check.result",
                                        Replacement.create("DONATOR", result.getDonator().getAsMention()),
                                        Replacement.create("RECEIVER", result.getReceiver().getAsMention())));
                eventWrapper.reply(match.build()).queue();
            }
            case ANSWER -> {
                var match = new LocalizedEmbedBuilder(loc, eventWrapper)
                        .setTitle("command.thankwords.sub.check.match.answer")
                        .setDescription(
                                eventWrapper.localize("command.thankwords.sub.check.result",
                                        Replacement.create("DONATOR", result.getDonator().getAsMention()),
                                        Replacement.create("RECEIVER", result.getReceiver().getAsMention())) + "\n"
                                        + eventWrapper.localize("command.thankwords.sub.check.reference",
                                        Replacement.create("URL", result.getReferenceMessage().getJumpUrl())));
                eventWrapper.reply(match.build()).queue();
            }
            case NO_MATCH -> eventWrapper.reply(eventWrapper.localize("command.thankwords.sub.check.match.noMatch")).queue();
        }
        return true;
    }
}
