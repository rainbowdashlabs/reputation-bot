package de.chojo.repbot.commands;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.data.GuildData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Locale;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class Thankwords extends SimpleCommand {

    private static final Logger log = getLogger(Thankwords.class);
    private final GuildData data;
    private final Localizer loc;
    private final ThankwordsContainer thankwordsContainer;

    private Thankwords(GuildData data, Localizer localizer, ThankwordsContainer thankwordsContainer) {
        super("thankwords", new String[]{"tw"},
                "command.thankwords.description",
                subCommandBuilder()
                        .add("add", "command.thankwords.sub.add", argsBuilder()
                                .add(OptionType.STRING, "pattern", "pattern", true)
                                .build())
                        .add("remove", "pattern", argsBuilder()
                                .add(OptionType.STRING, "pattern", "pattern", true)
                                .build())
                        .add("list", "command.thankwords.sub.list")
                        .add("check", "command.thankwords.sub.check", argsBuilder()
                                .add(OptionType.STRING, "sentence", "sentence")
                                .build()
                        )
                        .add("loaddefault", "command.thankwords.sub.loadDefault", argsBuilder()
                                .add(OptionType.STRING, "langauge", "language")
                                .build()
                        ).build(),
                Permission.MANAGE_SERVER);
        this.data = data;
        this.loc = localizer;
        this.thankwordsContainer = thankwordsContainer;
    }

    public static Thankwords of(DataSource dataSource, Localizer localizer) {
        ThankwordsContainer thankwordsContainer;
        try {
            thankwordsContainer = new ObjectMapper()
                    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                    .readValue(Thankwords.class.getClassLoader().getResourceAsStream("Thankswords.json"), ThankwordsContainer.class);
        } catch (IOException e) {
            thankwordsContainer = null;
            log.error("Could not read thankwords", e);
        }
        return new Thankwords(new GuildData(dataSource), localizer, thankwordsContainer);
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
        if ("loaddefaults".equalsIgnoreCase(subCmd)) {
            return loadDefaults(eventWrapper, context.subContext(subCmd));
        }
        return false;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {

    }

    private boolean add(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;

        var pattern = context.argString(0).get();
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.invalidRegex"), 30);
            return true;
        }
        if (data.addThankWord(eventWrapper.getGuild(), pattern)) {
            eventWrapper.reply(eventWrapper.localize("command.thankwords.sub.add.added",
                    Replacement.create("REGEX", pattern, Format.CODE))).queue();
        }
        return true;
    }

    private boolean remove(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;

        var pattern = context.argString(0).get();
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.invalidRegex"), 30);
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

        var result = MessageAnalyzer.processMessage(guildSettings.getThankwordPattern(), eventWrapper.getMessage(), guildSettings.getMaxMessageAge(), true, 0.85, 3);
        var builder = new LocalizedEmbedBuilder(eventWrapper);
        if (result.getReceivers().isEmpty()) {
            eventWrapper.reply(eventWrapper.localize("command.thankwords.sub.check.match.noMatch")).queue();
            return true;
        }

        for (var receiver : result.getReceivers()) {
            switch (result.getType()) {
                case FUZZY -> builder.addField("command.thankwords.sub.check.match.fuzzy",
                        eventWrapper.localize("command.thankwords.sub.check.result",
                                Replacement.create("DONATOR", result.getDonator().getAsMention()),
                                Replacement.create("RECEIVER", receiver.getReference().getAsMention())) + "\n"
                                + eventWrapper.localize("command.thankwords.sub.check.confidence",
                                Replacement.create("SCORE", String.format("%.3f", receiver.getWeight()))),
                        false);
                case MENTION -> builder.addField("command.thankwords.sub.check.match.mention",
                        eventWrapper.localize("command.thankwords.sub.check.result",
                                Replacement.create("DONATOR", result.getDonator().getAsMention()),
                                Replacement.create("RECEIVER", receiver.getReference().getAsMention())),
                        false);
                case ANSWER -> {
                    builder.addField("command.thankwords.sub.check.match.answer",
                            eventWrapper.localize("command.thankwords.sub.check.result",
                                    Replacement.create("DONATOR", result.getDonator().getAsMention()),
                                    Replacement.create("RECEIVER", receiver.getReference().getAsMention())) + "\n"
                                    + eventWrapper.localize("command.thankwords.sub.check.reference",
                                    Replacement.create("URL", result.getReferenceMessage().getJumpUrl())),
                            false);

                    var match = new LocalizedEmbedBuilder(loc, eventWrapper)
                            .setTitle("command.thankwords.sub.check.match.answer")
                            .setDescription(
                                    eventWrapper.localize("command.thankwords.sub.check.result",
                                            Replacement.create("DONATOR", result.getDonator().getAsMention()),
                                            Replacement.create("RECEIVER", receiver.getReference().getAsMention())) + "\n"
                                            + eventWrapper.localize("command.thankwords.sub.check.reference",
                                            Replacement.create("URL", result.getReferenceMessage().getJumpUrl())));
                    eventWrapper.reply(match.build()).queue();
                }
            }
        }

        eventWrapper.reply(builder.build()).queue();
        return true;
    }

    private boolean loadDefaults(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) {
            eventWrapper.reply(eventWrapper.localize("command.thankwords.sub.loadDefault.available")
                    + " " + String.join(", ", thankwordsContainer.getAvailableLanguages())).queue();
            return true;
        }
        var language = context.argString(0).get();
        var words = thankwordsContainer.get(language.toLowerCase(Locale.ROOT));
        if (words == null) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("command.locale.error.invalidLocale"), 10);
            return true;
        }
        for (var word : words) {
            data.addThankWord(eventWrapper.getGuild(), word);
        }

        var wordsJoined = words.stream().map(w -> StringUtils.wrap(w, "`")).collect(Collectors.joining(", "));

        eventWrapper.reply(eventWrapper.localize("command.thankwords.sub.loadDefault.added") + wordsJoined).queue();
        return true;
    }

    private static class ThankwordsContainer {
        private final Map<String, List<String>> defaults = new HashMap<>();

        public ThankwordsContainer() {
        }

        public List<String> get(String key) {
            return defaults.get(key);
        }

        public Set<String> getAvailableLanguages() {
            return Collections.unmodifiableSet(defaults.keySet());
        }
    }
}
