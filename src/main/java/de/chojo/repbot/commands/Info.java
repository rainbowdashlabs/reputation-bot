package de.chojo.repbot.commands;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.util.Colors;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class Info extends SimpleCommand {
    private static final String ART = "**SmartieFox ☆*:.｡.o(≧▽≦)o.｡.:*☆**\n[Twitter](https://twitter.com/smartiefoxart) [Twitch](https://www.twitch.tv/smartiefox)";
    private static final String SOURCE = "[rainbowdashlabs/reputation-bot](https://github.com/rainbowdashlabs/reputation-bot)";
    private static final Logger log = getLogger(Info.class);
    private final Localizer localizer;
    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private final ObjectMapper mapper = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final String version;
    private final Configuration configuration;
    private String contributors;
    private Instant lastFetch = Instant.MIN;


    public Info(Localizer localizer, String version, Configuration configuration) {
        super("info", null, "command.info.description", (SimpleArgument[]) null, Permission.UNKNOWN);
        this.localizer = localizer;
        this.version = version;
        this.configuration = configuration;
    }

    public static Info create(Localizer localizer, Configuration configuration) {
        var version = "undefined";
        try (var in = Info.class.getClassLoader().getResourceAsStream("version")) {
            version = new String(in.readAllBytes());
        } catch (IOException e) {
            log.error("Could not determine version.");
        }
        return new Info(localizer, version, configuration);
    }


    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        eventWrapper.reply(getResponse(eventWrapper))
                .queue();
        return true;
    }

    @NotNull
    private MessageEmbed getResponse(MessageEventWrapper eventWrapper) {
        if (contributors == null || lastFetch.isBefore(Instant.now().minus(5, ChronoUnit.MINUTES))) {
            var request = HttpRequest.newBuilder().GET()
                    .uri(URI.create("https://api.github.com/repos/rainbowdashlabs/reputation-bot/contributors?anon=1"))
                    .header("accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "reputation-bot")
                    .build();

            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                List<GithubProfile> contributorProfiles = mapper.readerForListOf(GithubProfile.class).readValue(response.body());
                contributors = contributorProfiles.stream().map(GithubProfile::toString).collect(Collectors.joining(", "));
            } catch (IOException | InterruptedException e) {
                log.error("Could not read response", e);
            }
            lastFetch = Instant.now();
        }

        return new LocalizedEmbedBuilder(localizer, eventWrapper)
                .setTitle("command.info.embedTitle")
                .setThumbnail(eventWrapper.getJda().getSelfUser().getEffectiveAvatarUrl())
                .addField("command.info.contributor", contributors, false)
                .addField("command.info.art", ART, true)
                .addField("command.info.source", SOURCE, true)
                .addField("command.info.version", version, true)
                .addField("", "**"+localizer.localize("command.info.links", eventWrapper.getGuild(),
                        Replacement.create("INVITE", configuration.links().invite()),
                        Replacement.create("SUPPORT", configuration.links().support()),
                        Replacement.create("TOS", configuration.links().tos()),
                        Replacement.create("WEBSITE", configuration.links().website())) + "**", false)
                .setColor(Colors.Pastel.BLUE)
                .build();
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        var eventWrapper = MessageEventWrapper.create(event);
        event.replyEmbeds(getResponse(eventWrapper)).queue();
    }


    private static class GithubProfile {
        private String login;
        @JsonProperty("html_url")
        private String htmlUrl;

        @Override
        public String toString() {
            return String.format("[%s](%s)", login, htmlUrl);
        }
    }
}
