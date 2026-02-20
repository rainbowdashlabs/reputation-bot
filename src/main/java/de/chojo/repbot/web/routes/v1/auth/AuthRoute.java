/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.auth;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.UserRepository;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.error.ErrorResponseWrapper;
import de.chojo.repbot.web.pojo.session.UserSessionPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.services.DiscordOAuthService;
import de.chojo.repbot.web.services.SessionService;
import io.javalin.http.Context;
import io.javalin.http.Cookie;
import io.javalin.http.HttpStatus;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class AuthRoute implements RoutesBuilder {
    private final DiscordOAuthService discordOAuthService;
    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final Configuration configuration;

    public AuthRoute(
            DiscordOAuthService discordOAuthService,
            UserRepository userRepository,
            SessionService sessionService,
            Configuration configuration) {
        this.discordOAuthService = discordOAuthService;
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.configuration = configuration;
    }

    @OpenApi(
            summary = "Start Discord OAuth login flow",
            operationId = "discordLogin",
            path = "v1/auth/discord/login",
            methods = HttpMethod.GET,
            tags = {"Auth"},
            responses = {@OpenApiResponse(status = "302", description = "Redirects to Discord authorize page.")})
    private void startDiscordLogin(Context ctx) {
        String next = ctx.queryParam("state");
        // Generate server-side random state to protect against CSRF
        String serverState = UUID.randomUUID().toString().replace("-", "");
        // Persist server state in a short-lived cookie for later validation
        Cookie cookie = new Cookie("oauth_state", serverState);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(300);
        ctx.cookie(cookie);
        // Compose final state: <serverState>::<nextPath>
        String composedState = serverState + "::" + (next != null ? next : "/");
        String url = discordOAuthService.buildAuthorizeUrl(composedState);
        ctx.redirect(url);
    }

    @OpenApi(
            summary = "OAuth2 callback endpoint for Discord",
            operationId = "discordCallback",
            path = "v1/auth/discord/callback",
            methods = HttpMethod.GET,
            tags = {"Auth"},
            queryParams = {
                @OpenApiParam(name = "code", required = true, description = "Authorization code from Discord"),
                @OpenApiParam(name = "state", required = false, description = "Opaque state passed through")
            },
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = UserSessionPOJO.class, type = "application/json")}),
                @OpenApiResponse(status = "400", description = "Invalid request")
            })
    private void discordCallback(Context ctx) {
        String code = ctx.queryParam("code");
        String state = ctx.queryParam("state");
        if (code == null || code.isBlank()) {
            ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseWrapper("Invalid Request", "Missing code"));
            return;
        }
        if (state == null || state.isBlank()) {
            ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseWrapper("Invalid Request", "Missing state"));
            return;
        }
        try {
            // Validate state against cookie
            String cookieState = ctx.cookie("oauth_state");
            String serverState;
            String nextPath = "/";
            int idx = state.indexOf("::");
            if (idx > 0) {
                serverState = state.substring(0, idx);
                nextPath = state.substring(idx + 2);
            } else {
                serverState = state;
            }
            if (cookieState == null || !cookieState.equals(serverState)) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(new ErrorResponseWrapper("Invalid State", "OAuth state validation failed"));
                return;
            }

            var token = discordOAuthService.exchangeCode(code);
            long userId = discordOAuthService.getCurrentUserId(token.accessToken());
            userRepository.updateToken(userId, token.accessToken(), token.refreshToken(), token.expiry());
            var session = sessionService.createSession(userId);

            // Redirect back to frontend with session token
            String base = configuration.api().url();
            if (nextPath == null || nextPath.isBlank() || !nextPath.startsWith("/")) {
                nextPath = "/";
            }
            String separator = nextPath.contains("?") ? "&" : "?";
            String redirectUrl;
            if (base.endsWith("/") && nextPath.startsWith("/")) {
                redirectUrl = base + nextPath.substring(1) + separator + "token="
                        + URLEncoder.encode(session.token(), StandardCharsets.UTF_8);
            } else if (!base.endsWith("/") && !nextPath.startsWith("/")) {
                redirectUrl = base + "/" + nextPath + separator + "token="
                        + URLEncoder.encode(session.token(), StandardCharsets.UTF_8);
            } else {
                redirectUrl = base + nextPath + separator + "token="
                        + URLEncoder.encode(session.token(), StandardCharsets.UTF_8);
            }
            // Clear state cookie
            ctx.removeCookie("oauth_state", "/");
            ctx.redirect(redirectUrl);
        } catch (Exception e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponseWrapper("Discord OAuth Failed", e.getMessage()));
        }
    }

    @OpenApi(
            summary = "Logout and revoke Discord OAuth token",
            operationId = "logout",
            path = "v1/auth/logout",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = false, description = "User Session Token")},
            tags = {"Auth"},
            responses = {
                @OpenApiResponse(status = "204", description = "Session invalidated (token revoked if possible)")
            })
    private void logout(Context ctx) {
        String token = ctx.header("Authorization");
        if (token == null || token.isBlank()) {
            ctx.status(HttpStatus.NO_CONTENT);
            return;
        }
        try {
            // Try to revoke the Discord OAuth token of the logged-in user
            sessionService.getUserSession(ctx).ifPresent(userSession -> {
                userRepository.token(userSession.userId()).ifPresent(storedToken -> {
                    try {
                        discordOAuthService.revokeToken(storedToken.accessToken());
                    } catch (Exception ignore) {
                        // Swallow errors on revoke to still allow logout
                    }
                });
            });
        } catch (Exception ignore) {
            // ignore revoke issues
        } finally {
            sessionService.logout(token);
        }
        ctx.status(HttpStatus.NO_CONTENT);
    }

    @OpenApi(
            summary = "Validate user session token",
            operationId = "validate",
            path = "v1/auth/validate",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "User Session Token")},
            tags = {"Auth"},
            responses = {
                @OpenApiResponse(status = "204", description = "Token is valid"),
                @OpenApiResponse(status = "401", description = "Token is invalid")
            })
    private void validateToken(Context ctx) {
        if (sessionService.getUserSession(ctx).isPresent()) {
            ctx.status(HttpStatus.NO_CONTENT);
        } else {
            throw new UnauthorizedResponse("Invalid session token");
        }
    }

    @Override
    public void buildRoutes() {
        path("auth", () -> {
            get("validate", this::validateToken, Role.ANYONE);
            path("discord", () -> {
                get("login", this::startDiscordLogin, Role.ANYONE);
                path("callback", () -> get(this::discordCallback, Role.ANYONE));
            });
            post("logout", this::logout, Role.ANYONE);
        });
    }
}
