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
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
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
import java.util.Objects;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class Info extends SimpleCommand {
    private static final String ART = "**SmartieFox ☆*:.｡.o(≧▽≦)o.｡.:*☆**\n[Twitter](https://twitter.com/smartiefoxart) [Twitch](https://www.twitch.tv/smartiefox)";
    private static final String SOURCE = "[rainbowdashlabs/reputation-bot](https://github.com/repos/rainbowdashlabs/reputation-bot)";
    private static final Logger log = getLogger(Info.class);
    private String contributors;
    private final Localizer localizer;
    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private final ObjectMapper mapper = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final String version;
    private Instant lastFetch = Instant.MIN;


    public Info(Localizer localizer, String version) {
        super("info", null, "command.info.description", (SimpleArgument[]) null, Permission.UNKNOWN);
        this.localizer = localizer;
        this.version = version;
    }

    public static Info create(Localizer localizer) {
        var version = "undefined";
        try (var in = Info.class.getClassLoader().getResourceAsStream("version")) {
            version = new String(in.readAllBytes());
        } catch (IOException e) {
            log.error("Could not determine version.");
        }
        return new Info(localizer, version);
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
        }

        return new LocalizedEmbedBuilder(localizer, eventWrapper)
                .setTitle("command.info.embedTitle")
                .setThumbnail(eventWrapper.getJda().getSelfUser().getEffectiveAvatarUrl())
                .addField("command.info.contributor", contributors, false)
                .addField("command.info.art", ART, true)
                .addField("command.info.source", SOURCE, true)
                .addField("command.info.version", version, true)
                .setColor(Colors.Pastel.BLUE)
                .build();
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        var eventWrapper = MessageEventWrapper.create(event);
        event.reply(wrap(getResponse(eventWrapper))).queue();
    }


    private static class GithubProfile {
        private String login;
        @JsonProperty("html_url")
        private String htmlUrl;

        @Override
        public String toString() {
            return String.format("[%s](%s)", login, htmlUrl);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            var that = (GithubProfile) o;

            if (!Objects.equals(login, that.login)) return false;
            return Objects.equals(htmlUrl, that.htmlUrl);
        }

        @Override
        public int hashCode() {
            int result = login != null ? login.hashCode() : 0;
            result = 31 * result + (htmlUrl != null ? htmlUrl.hashCode() : 0);
            return result;
        }
    }
}
