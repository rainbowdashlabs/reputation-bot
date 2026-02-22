/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.user;

import de.chojo.jdautil.botlist.BotListConfig;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.vote.VoteLog;
import de.chojo.repbot.dao.access.vote.VoteStreak;
import de.chojo.repbot.dao.provider.VoteRepository;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.user.BotlistVotePOJO;
import de.chojo.repbot.web.pojo.user.UserTokensPOJO;
import de.chojo.repbot.web.pojo.user.VoteLogPagePOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.services.UserSession;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class UserRoute implements RoutesBuilder {
    private final VoteRepository voteRepository;
    private final Configuration configuration;

    public UserRoute(VoteRepository voteRepository, Configuration configuration) {
        this.voteRepository = voteRepository;
        this.configuration = configuration;
    }

    @OpenApi(
            summary = "Get the current tokens of a user.",
            operationId = "getUserTokens",
            path = "v1/user/vote/tokens",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "User Session Token")},
            tags = {"User"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = UserTokensPOJO.class, type = "application/json")})
            })
    private void getUserTokens(@NotNull Context ctx) {
        UserSession session = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        int tokens = voteRepository.getUserToken(session.userId());
        ctx.json(new UserTokensPOJO(tokens));
    }

    @OpenApi(
            summary = "Get the voting information of a user.",
            operationId = "getUserVoteLists",
            path = "v1/user/vote/lists",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "User Session Token")},
            tags = {"User"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = BotlistVotePOJO[].class, type = "application/json")})
            })
    private void getUserVoteLists(@NotNull Context ctx) {
        UserSession session = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        List<BotlistVotePOJO> votes = new ArrayList<>();
        for (BotListConfig botlist : configuration.botlist().botlists()) {
            if (botlist.voteUrl().isBlank()) continue;
            VoteStreak streak = voteRepository.getLastVote(session.userId(), botlist.name());
            votes.add(new BotlistVotePOJO(botlist.name(), botlist.voteUrl(), streak.lastVote(), streak.streak()));
        }
        ctx.json(votes);
    }

    @OpenApi(
            summary = "Get vote log",
            operationId = "getVoteLog",
            path = "v1/user/vote/log",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "User Session Token")},
            queryParams = {
                @OpenApiParam(name = "page", description = "Page number (default: 0)"),
                @OpenApiParam(name = "entries", description = "Entries per page (default: 25)")
            },
            tags = {"User"},
            responses = {@OpenApiResponse(status = "200", content = @OpenApiContent(from = VoteLogPagePOJO.class))})
    private void getVoteLog(@NotNull Context ctx) {
        UserSession session = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
        int entries = ctx.queryParamAsClass("entries", Integer.class).getOrDefault(25);

        List<VoteLog> voteLogs = voteRepository.getVoteLog(session.userId(), page, entries);
        long maxPages = voteRepository.getVoteLogPages(session.userId(), entries);

        ctx.json(new VoteLogPagePOJO(page, maxPages, voteLogs));
    }

    @Override
    public void buildRoutes() {
        path("user", () -> {
            path("vote", () -> {
                get("tokens", this::getUserTokens, Role.USER);
                get("lists", this::getUserVoteLists, Role.USER);
                get("log", this::getVoteLog, Role.USER);
            });
        });
    }
}
