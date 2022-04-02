package de.chojo.repbot.commands;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.Verifier;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.serialization.ThankwordsContainer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class Thankwords extends SimpleCommand {

    private static final Logger log = getLogger(Thankwords.class);
    private final GuildData data;
    private final ThankwordsContainer thankwordsContainer;
    private final MessageAnalyzer messageAnalyzer;

    private Thankwords(MessageAnalyzer messageAnalyzer, GuildData data, ThankwordsContainer thankwordsContainer) {
        super(CommandMeta.builder("thankwords", "command.thankwords.description")
                .addSubCommand("add", "command.thankwords.sub.add", argsBuilder()
                        .add(SimpleArgument.string("pattern", "pattern").asRequired()))
                .addSubCommand("remove", "pattern", argsBuilder()
                        .add(SimpleArgument.string("pattern", "pattern").asRequired()))
                .addSubCommand("list", "command.thankwords.sub.list")
                .addSubCommand("check", "command.thankwords.sub.check", argsBuilder()
                        .add(SimpleArgument.string("message", "message").asRequired()))
                .addSubCommand("loaddefault", "command.thankwords.sub.loadDefault", argsBuilder()
                        .add(SimpleArgument.string("language", "language")))
                .withPermission());
        this.data = data;
        this.thankwordsContainer = thankwordsContainer;
        this.messageAnalyzer = messageAnalyzer;
    }

    public static Thankwords of(MessageAnalyzer messageAnalyzer, DataSource dataSource) {
        ThankwordsContainer thankwordsContainer;
        try {
            thankwordsContainer = loadContainer();
        } catch (IOException e) {
            thankwordsContainer = null;
            log.error("Could not read thankwords", e);
        }
        return new Thankwords(messageAnalyzer, new GuildData(dataSource), thankwordsContainer);
    }

    public static ThankwordsContainer loadContainer() throws IOException {
        try (var input = Thankwords.class.getClassLoader().getResourceAsStream("Thankswords.json")) {
            return new ObjectMapper()
                    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                    .readValue(input, ThankwordsContainer.class);
        }
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var subCmd = event.getSubcommandName();
        if ("add".equalsIgnoreCase(subCmd)) {
            add(event, context);
        }
        if ("remove".equalsIgnoreCase(subCmd)) {
            remove(event, context);
        }
        if ("list".equalsIgnoreCase(subCmd)) {
            list(event, context);
        }
        if ("check".equalsIgnoreCase(subCmd)) {
            check(event, context);
        }
        if ("loaddefault".equalsIgnoreCase(subCmd)) {
            loadDefaults(event, context);
        }
    }

    private boolean add(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var pattern = event.getOption("pattern").getAsString();
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            event.reply(context.localize("error.invalidRegex"))
                    .setEphemeral(true)
                    .queue();
            return true;
        }
        if (data.addThankWord(event.getGuild(), pattern)) {
            event.reply(context.localize("command.thankwords.sub.add.added",
                    Replacement.create("REGEX", pattern, Format.CODE))).queue();
        }
        return true;
    }

    private boolean remove(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var pattern = event.getOption("pattern").getAsString();
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            event.reply(context.localize("error.invalidRegex"))
                    .setEphemeral(true)
                    .queue();
            return true;
        }
        if (data.removeThankWord(event.getGuild(), pattern)) {
            event.reply(context.localize("command.thankwords.sub.remove.removed",
                    Replacement.create("PATTERN", pattern, Format.CODE))).queue();
            return true;
        }
        event.reply(context.localize("command.thankwords.error.patternNotFound"))
                .setEphemeral(true)
                .queue();
        return true;
    }

    private boolean list(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var pattern = getGuildPattern(event.getGuild());
        if (pattern == null) return false;

        event.reply(context.localize("command.thankwords.sub.list.list") + "\n" + pattern).queue();
        return true;
    }

    @Nullable
    private String getGuildPattern(Guild guild) {

        var guildSettings = data.getGuildSettings(guild);

        var pattern = Arrays.stream(guildSettings.thankSettings().thankwords())
                .map(w -> StringUtils.wrap(w, "`"))
                .collect(Collectors.joining(", "));
        return pattern;
    }

    private boolean check(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var guildSettings = data.getGuildSettings(event.getGuild());
        var messageId = event.getOption("message").getAsString();

        if (!Verifier.isValidId(messageId)) {
            event.reply(context.localize("error.invalidMessage")).queue();
            return true;
        }

        var message = event.getChannel().retrieveMessageById(messageId).complete();
        var result = messageAnalyzer.processMessage(guildSettings.thankSettings().thankwordPattern(), message, guildSettings, true, 3);
        if (result.receivers().isEmpty()) {
            event.reply(context.localize("command.thankwords.sub.check.match.noMatch")).queue();
            return true;
        }

        var builder = new LocalizedEmbedBuilder(context.localizer());
        processMessage(result, builder);
        event.replyEmbeds(builder.build()).queue();
        return true;
    }

    private void processMessage(de.chojo.repbot.analyzer.AnalyzerResult result, LocalizedEmbedBuilder builder) {
        for (var receiver : result.receivers()) {
            switch (result.type()) {
                case FUZZY -> builder.addField("command.thankwords.sub.check.match.fuzzy",
                        "$command.thankwords.sub.check.result$"
                        + "\n" +
                        "$command.thankwords.sub.check.confidence$",
                        false, Replacement.create("DONATOR", result.donator().getAsMention()),
                        Replacement.create("RECEIVER", receiver.getReference().getAsMention()),
                        Replacement.create("SCORE", String.format("%.3f", receiver.getWeight())));
                case MENTION -> builder.addField("command.thankwords.sub.check.match.mention",
                        "command.thankwords.sub.check.result",
                        false, Replacement.create("DONATOR", result.donator().getAsMention()),
                        Replacement.create("RECEIVER", receiver.getReference().getAsMention()));
                case ANSWER -> builder.addField("command.thankwords.sub.check.match.answer",
                        "$command.thankwords.sub.check.result$" + "\n"+"$command.thankwords.sub.check.reference$",
                        false, Replacement.create("URL", result.referenceMessage().getJumpUrl()),
                        Replacement.create("DONATOR", result.donator().getAsMention()),
                        Replacement.create("RECEIVER", receiver.getReference().getAsMention()));
            }
        }
    }

    private boolean loadDefaults(SlashCommandInteractionEvent slashCommandEvent, SlashCommandContext context) {
        var languageOption = slashCommandEvent.getOption("language");
        if (languageOption == null) {
            slashCommandEvent.reply(context.localize("command.thankwords.sub.loadDefault.available")
                                    + " " + String.join(", ", thankwordsContainer.getAvailableLanguages())).queue();
            return true;
        }
        var language = languageOption.getAsString();
        var words = thankwordsContainer.get(language.toLowerCase(Locale.ROOT));
        if (words == null) {
            slashCommandEvent.reply(context.localize("command.locale.error.invalidLocale"))
                    .setEphemeral(true)
                    .queue();
            return true;
        }
        for (var word : words) {
            data.addThankWord(slashCommandEvent.getGuild(), word);
        }

        var wordsJoined = words.stream().map(w -> StringUtils.wrap(w, "`")).collect(Collectors.joining(", "));

        slashCommandEvent.reply(context.localize("command.thankwords.sub.loadDefault.added") + wordsJoined).queue();
        return true;
    }
}
