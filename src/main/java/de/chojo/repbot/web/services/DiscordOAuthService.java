/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.config.elements.DiscordOAuth;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

public class DiscordOAuthService {
    private static final String DISCORD_BASE = "https://discord.com";
    private static final String DISCORD_API = DISCORD_BASE + "/api/";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final Configuration configuration;

    public DiscordOAuthService(Configuration configuration) {
        this.configuration = configuration;
    }

    public String buildAuthorizeUrl(String state) {
        String base = DISCORD_BASE + "/oauth2/authorize";
        String params = "client_id=" + enc(cfg().clientId())
                + "&redirect_uri=" + enc(cfg().redirectUri())
                + "&response_type=code"
                + "&prompt=none"
                + "&scope=" + enc("identify guilds")
                + (state != null ? "&state=" + enc(state) : "");
        return base + "?" + params;
    }

    public TokenResponse exchangeCode(String code) throws IOException, InterruptedException {
        String form =
                "grant_type=authorization_code" + "&code=" + enc(code) + "&redirect_uri=" + enc(cfg().redirectUri());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DISCORD_API + "/oauth2/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", basicAuth(cfg().clientId(), cfg().clientSecret()))
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("Discord token exchange failed: " + response.statusCode() + " - " + response.body());
        }
        return mapper.readValue(response.body(), DiscordTokenResponse.class).toTokenResponse();
    }

    public long getCurrentUserId(String accessToken) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DISCORD_API + "/users/@me"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("Discord /users/@me failed: " + response.statusCode() + " - " + response.body());
        }
        return mapper.readValue(response.body(), DiscordUser.class).id();
    }

    public record DiscordGuild(
            String id,
            String name,
            String icon,
            String permissions,
            @JsonProperty("permissions_new") String permissionsNew,
            boolean owner) {}

    public List<DiscordGuild> getUserGuilds(String accessToken) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DISCORD_API + "/users/@me/guilds"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IOException(
                    "Discord /users/@me/guilds failed: " + response.statusCode() + " - " + response.body());
        }
        return mapper.readValue(response.body(), new TypeReference<>() {});
    }

    /**
     * Revokes a Discord OAuth token for the current application.
     * See https://discord.com/developers/docs/topics/oauth2#revoking-tokens
     */
    public void revokeToken(String token) throws IOException, InterruptedException {
        String form = "token=" + enc(token);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DISCORD_API + "/oauth2/token/revoke"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", basicAuth(cfg().clientId(), cfg().clientSecret()))
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("Discord token revoke failed: " + response.statusCode() + " - " + response.body());
        }
    }

    private String basicAuth(String username, String password) {
        String auth = username + ":" + password;
        return "Basic " + java.util.Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    private DiscordOAuth cfg() {
        return configuration.discordOAuth();
    }

    private static String enc(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    public record TokenResponse(String accessToken, String refreshToken, Instant expiry) {}

    private record DiscordTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("expires_in") long expiresIn) {
        public TokenResponse toTokenResponse() {
            return new TokenResponse(accessToken, refreshToken, Instant.now().plusSeconds(expiresIn));
        }
    }

    private record DiscordUser(long id) {}
}
