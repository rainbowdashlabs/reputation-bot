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
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.serialization.ThankwordsContainer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
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
    private final GuildData guildData;
    private final ThankwordsContainer thankwordsContainer;
    private final MessageAnalyzer messageAnalyzer;

    private Thankwords(MessageAnalyzer messageAnalyzer, GuildData guildData, ThankwordsContainer thankwordsContainer) {
        super(CommandMeta.builder("thankwords", "command.thankwords.description")
                .addSubCommand("add", "command.thankwords.sub.add", argsBuilder()
                        .add(SimpleArgument.string("pattern", "command.thankwords.sub.add.arg.pattern").asRequired()))
                .addSubCommand("remove", "pattern", argsBuilder()
                        .add(SimpleArgument.string("pattern", "command.thankwords.sub.remove.arg.pattern").asRequired().withAutoComplete()))
                .addSubCommand("list", "command.thankwords.sub.list")
                .addSubCommand("check", "command.thankwords.sub.check", argsBuilder()
                        .add(SimpleArgument.string("message", "command.thankwords.sub.check.arg.message").asRequired()))
                .addSubCommand("loaddefault", "command.thankwords.sub.loadDefault", argsBuilder()
                        .add(SimpleArgument.string("language", "command.thankwords.sub.loadDefault.arg.language").withAutoComplete()))
                .withPermission());
        this.guildData = guildData;
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

    private void add(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var pattern = event.getOption("pattern").getAsString();
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            event.reply(context.localize("error.invalidRegex"))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (guildData.addThankWord(event.getGuild(), pattern)) {
            event.reply(context.localize("command.thankwords.sub.add.added",
                    Replacement.create("REGEX", pattern, Format.CODE))).queue();
        }
    }

    private void remove(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var pattern = event.getOption("pattern").getAsString();
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            event.reply(context.localize("error.invalidRegex"))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (guildData.removeThankWord(event.getGuild(), pattern)) {
            event.reply(context.localize("command.thankwords.sub.remove.removed",
                    Replacement.create("PATTERN", pattern, Format.CODE))).queue();
            return;
        }
        event.reply(context.localize("command.thankwords.error.patternNotFound"))
                .setEphemeral(true)
                .queue();
    }

    private void list(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var pattern = getGuildPattern(event.getGuild());
        if (pattern == null) return;

        event.reply(context.localize("command.thankwords.sub.list.list") + "\n" + pattern).queue();
    }

    @Nullable
    private String getGuildPattern(Guild guild) {

        var guildSettings = guildData.getGuildSettings(guild);

        return Arrays.stream(guildSettings.thankSettings().thankwords())
                .map(w -> StringUtils.wrap(w, "`"))
                .collect(Collectors.joining(", "));
    }

    private void check(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var guildSettings = guildData.getGuildSettings(event.getGuild());
        var messageId = event.getOption("message").getAsString();

        if (!Verifier.isValidId(messageId)) {
            event.reply(context.localize("error.invalidMessage")).queue();
            return;
        }

        var message = event.getChannel().retrieveMessageById(messageId).complete();
        var result = messageAnalyzer.processMessage(guildSettings.thankSettings().thankwordPattern(), message, guildSettings, true, 3);
        if (result.receivers().isEmpty()) {
            event.reply(context.localize("command.thankwords.sub.check.match.noMatch")).queue();
            return;
        }

        var builder = new LocalizedEmbedBuilder(context.localizer());
        processMessage(result, builder);
        event.replyEmbeds(builder.build()).queue();
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
                        "$command.thankwords.sub.check.result$\n$command.thankwords.sub.check.reference$",
                        false, Replacement.create("URL", result.referenceMessage().getJumpUrl()),
                        Replacement.create("DONATOR", result.donator().getAsMention()),
                        Replacement.create("RECEIVER", receiver.getReference().getAsMention()));
            }
        }
    }

    private void loadDefaults(SlashCommandInteractionEvent slashCommandEvent, SlashCommandContext context) {
        var languageOption = slashCommandEvent.getOption("language");
        if (languageOption == null) {
            slashCommandEvent.reply(context.localize("command.thankwords.sub.loadDefault.available")
                                    + " " + String.join(", ", thankwordsContainer.getAvailableLanguages())).queue();
            return;
        }
        var language = languageOption.getAsString();
        var words = thankwordsContainer.get(language.toLowerCase(Locale.ROOT));
        if (words == null) {
            slashCommandEvent.reply(context.localize("command.locale.error.invalidLocale"))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        for (var word : words) {
            guildData.addThankWord(slashCommandEvent.getGuild(), word);
        }

        var wordsJoined = words.stream().map(w -> StringUtils.wrap(w, "`")).collect(Collectors.joining(", "));

        slashCommandEvent.reply(context.localize("command.thankwords.sub.loadDefault.added") + wordsJoined).queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, SlashCommandContext slashCommandContext) {
        var option = event.getFocusedOption();
        var cmd = event.getSubcommandName();
        if ("remove".equals(cmd) && "pattern".equals(option.getName())) {
            var thankwords = guildData.getGuildSettings(event.getGuild()).thankSettings().thankwords();
            event.replyChoices(Completion.complete(option.getValue(), thankwords)).queue();
        }
        if ("loaddefault".equals(cmd) && "language".equals(option.getName())) {
            event.replyChoices(Completion.complete(option.getValue(), thankwordsContainer.getAvailableLanguages())).queue();
        }
    }
}
