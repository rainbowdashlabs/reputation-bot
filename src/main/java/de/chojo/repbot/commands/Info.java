package de.chojo.repbot.commands;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.util.Colors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class Info extends SimpleCommand {
    private static final String ART = "**SmartieFox ☆*:.｡.o(≧▽≦)o.｡.:*☆**\n[Twitter](https://twitter.com/smartiefoxart) [Twitch](https://www.twitch.tv/smartiefox)";
    private static final String SOURCE = "[rainbowdashlabs/reputation-bot](https://github.com/rainbowdashlabs/reputation-bot)";
    private static final Logger log = getLogger(Info.class);
    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private final ObjectMapper mapper = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final String version;
    private final Configuration configuration;
    private String contributors;
    private Instant lastFetch = Instant.MIN;


    public Info(String version, Configuration configuration) {
        super(CommandMeta.builder("info", "command.info.description"));
        this.version = version;
        this.configuration = configuration;
    }

    public static Info create(Configuration configuration) {
        var version = "undefined";
        try (var in = Info.class.getClassLoader().getResourceAsStream("version")) {
            version = new String(in.readAllBytes()).trim();
        } catch (IOException e) {
            log.error("Could not determine version.");
        }
        return new Info(version, configuration);
    }


    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        event.replyEmbeds(getResponse(event, context)).queue();
    }

    @NotNull
    private MessageEmbed getResponse(SlashCommandInteractionEvent event, SlashCommandContext context) {
        if (contributors == null || lastFetch.isBefore(Instant.now().minus(5, ChronoUnit.MINUTES))) {
            var request = HttpRequest.newBuilder().GET()
                    .uri(URI.create("https://api.github.com/repos/rainbowdashlabs/reputation-bot/contributors?anon=1"))
                    .header("accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "reputation-bot")
                    .build();

            List<Contributor> contributors;
            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                contributors = mapper.readerForListOf(Contributor.class).readValue(response.body());
            } catch (IOException | InterruptedException e) {
                log.error("Could not read response", e);
                return new EmbedBuilder().build();
            }

            List<GithubProfile> profiles = new ArrayList<>();
            for (var contributor : contributors) {
                if (ContributorType.BOT == contributor.type) continue;

                var profile = HttpRequest.newBuilder().GET()
                        .uri(URI.create(contributor.url))
                        .header("accept", "application/vnd.github.v3+json")
                        .header("User-Agent", "reputation-bot")
                        .build();

                try {
                    var response = client.send(profile, HttpResponse.BodyHandlers.ofString());
                    profiles.add(mapper.readValue(response.body(), GithubProfile.class));
                } catch (IOException | InterruptedException e) {
                    log.error("Could not read response", e);
                    return new EmbedBuilder().build();
                }
            }

            this.contributors = profiles.stream().map(GithubProfile::toString).collect(Collectors.joining(", "));

            lastFetch = Instant.now();
        }

        return new LocalizedEmbedBuilder(context.localizer())
                .setTitle("command.info.embedTitle")
                .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .addField("command.info.contributor", contributors, false)
                .addField("command.info.art", ART, true)
                .addField("command.info.source", SOURCE, true)
                .addField("command.info.version", version, true)
                .addField("command.info.supportMe", getVoting(context), true)
                .addField("", "**" + getLinks(context) + "**", false)
                .setColor(Colors.Pastel.BLUE)
                .build();
    }

    private String getLinks(SlashCommandContext context) {
        var links = List.of(
                getLink(context, "command.info.inviteMe", configuration.links().invite()),
                getLink(context, "command.info.support", configuration.links().support()),
                getLink(context, "command.info.tos", configuration.links().tos()),
                getLink(context, "command.info.website", configuration.links().website()),
                getLink(context, "command.info.faq", configuration.links().faq())
        );
        return String.join(" ᠅ ", links);
    }

    private String getLink(SlashCommandContext context, @PropertyKey(resourceBundle = "locale") String target, String url) {
        return context.localize("words.link", Replacement.create("TARGET", String.format("$%s$", target)),
                Replacement.create("URL", url));
    }

    private String getUntranslatedLink(SlashCommandContext context, String target, String url) {
        return context.localize("words.link",
                Replacement.create("TARGET", target),
                Replacement.create("URL", url));
    }

    private String getVoting(SlashCommandContext context) {
        List<String> voteLinks = new ArrayList<>();

        for (var botlist : configuration.botlist().botlists()) {
            if (botlist.voteUrl().isEmpty()) continue;
            voteLinks.add(getUntranslatedLink(context, botlist.name(), botlist.profileUrl() + "?ref=repbot"));
        }
        return String.join(" ᠅ ", voteLinks);
    }

    private enum ContributorType {
        @JsonProperty("User")
        USER,
        @JsonProperty("Bot")
        BOT
    }

    private static class Contributor {
        private String login;
        private String url;
        @JsonProperty("html_url")
        private String htmlUrl;
        private ContributorType type;
    }

    private static class GithubProfile {
        private String login;
        private String name;
        @JsonProperty("html_url")
        private String htmlUrl;

        @Override
        public String toString() {
            return String.format("[%s](%s)", name == null ? login : name, htmlUrl);
        }
    }
}
