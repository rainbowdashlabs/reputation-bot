/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.info.handler;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.util.Colors;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class Show implements SlashHandler {
    private static final String ART =
            "**SmartieFox ☆*:.｡.o(≧▽≦)o.｡.:*☆**\n[Twitter](https://twitter.com/smartiefoxart) [Twitch](https://www.twitch.tv/smartiefox)";
    private static final String SOURCE =
            "[rainbowdashlabs/reputation-bot](https://github.com/rainbowdashlabs/reputation-bot)";
    private static final Logger log = getLogger(Show.class);
    private final HttpClient client =
            HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private final ObjectMapper mapper = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final Path CACHE_FILE = Path.of("config", "contributors.json");
    private final String version;
    private final Configuration configuration;
    private String contributors = "";
    private Instant lastFetch = Instant.MIN;

    public Show(String version, Configuration configuration) {
        this.version = version;
        this.configuration = configuration;
        loadCache();
        refreshData();
    }

    private void loadCache() {
        if (!Files.exists(CACHE_FILE)) return;
        try {
            contributors = mapper.readValue(Files.readString(CACHE_FILE), String.class);
            lastFetch = Instant.now();
        } catch (IOException e) {
            log.error("Could not load contributors cache", e);
        }
    }

    private void saveCache(String data) {
        try {
            Files.createDirectories(CACHE_FILE.getParent());
            Files.writeString(CACHE_FILE, mapper.writeValueAsString(data));
        } catch (IOException e) {
            log.error("Could not save contributors cache", e);
        }
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        event.replyEmbeds(getResponse(event, context)).complete();
    }

    @NotNull
    private MessageEmbed getResponse(SlashCommandInteractionEvent event, EventContext context) {
        if (contributors == null || lastFetch.isBefore(Instant.now().minus(1, ChronoUnit.HOURS))) {
            CompletableFuture.runAsync(this::refreshData);
        }

        return new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.info.message.title")
                .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .addField("command.info.message.contributor", contributors, false)
                .addField("command.info.message.art", ART, true)
                .addField("command.info.message.source", SOURCE, true)
                .addField("command.info.message.version", version, true)
                .addField("command.info.message.supportMe", getVoting(context), true)
                .addField("", "**" + getLinks(context) + "**", false)
                .setColor(Colors.Pastel.BLUE)
                .build();
    }

    private void refreshData() {
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://api.github.com/repos/rainbowdashlabs/reputation-bot/contributors?anon=1"))
                .header("accept", "application/vnd.github.v3+json")
                .header("User-Agent", "reputation-bot")
                .build();

        List<Contributor> contributorsList;
        String body = null;
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("GitHub API returned status code {}. Body: {}", response.statusCode(), response.body());
                return;
            }
            body = response.body();
            contributorsList = mapper.readerForListOf(Contributor.class).readValue(body);
        } catch (IOException | InterruptedException e) {
            log.error("Could not read response", e);
            log.error("Body: {}", body);
            return;
        }

        List<GithubProfile> profiles = new ArrayList<>();
        boolean success = true;
        for (var contributor : contributorsList) {
            if (ContributorType.BOT == contributor.type) continue;

            var profileRequest = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(contributor.url))
                    .header("accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "reputation-bot")
                    .build();

            try {
                var response = client.send(profileRequest, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    log.error(
                            "GitHub API returned status code {} for profile {}",
                            response.statusCode(),
                            contributor.url);
                    success = false;
                    break;
                }
                profiles.add(mapper.readValue(response.body(), GithubProfile.class));
            } catch (IOException | InterruptedException e) {
                log.error("Could not read response for profile {}", contributor.url, e);
                success = false;
                break;
            }
        }

        if (success && !profiles.isEmpty()) {
            this.contributors = profiles.stream().map(GithubProfile::toString).collect(Collectors.joining(", "));
            lastFetch = Instant.now();
            saveCache(this.contributors);
        }
    }

    private String getLinks(EventContext context) {
        var links = List.of(
                getLink(
                        context,
                        "command.info.message.inviteme",
                        configuration.links().invite()),
                getLink(
                        context,
                        "command.info.message.support",
                        configuration.links().support()),
                getLink(
                        context,
                        "command.info.message.tos",
                        configuration.links().tos()),
                getLink(
                        context,
                        "command.info.message.website",
                        configuration.links().website()),
                getLink(
                        context,
                        "command.info.message.faq",
                        configuration.links().faq()));
        return String.join(" ᠅ ", links);
    }

    private String getLink(EventContext context, @PropertyKey(resourceBundle = "locale") String target, String url) {
        return context.localize(
                "words.link",
                Replacement.create("TARGET", String.format("$%s$", target)),
                Replacement.create("URL", url));
    }

    private String getUntranslatedLink(EventContext context, String target, String url) {
        return context.localize("words.link", Replacement.create("TARGET", target), Replacement.create("URL", url));
    }

    private String getVoting(EventContext context) {
        List<String> voteLinks = new ArrayList<>();

        for (var botlist : configuration.botlist().botlists()) {
            if (botlist.voteUrl().isEmpty()) continue;
            voteLinks.add(getUntranslatedLink(context, botlist.name(), botlist.profileUrl() + "?ref=repbot"));
        }
        return String.join(" ᠅ ", voteLinks);
    }

    @SuppressWarnings("unused")
    private enum ContributorType {
        @JsonProperty("User")
        USER,
        @JsonProperty("Bot")
        BOT
    }

    @SuppressWarnings("unused")
    private static class Contributor {
        private String login;
        private String url;

        @JsonProperty("html_url")
        private String htmlUrl;

        private ContributorType type;
    }

    @SuppressWarnings("unused")
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
