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
import de.chojo.repbot.web.pojo.user.TokensPOJO;
import de.chojo.repbot.web.pojo.user.VoteLogPagePOJO;
import de.chojo.repbot.web.pojo.user.VoteTransferRequestPOJO;
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
import static io.javalin.apibuilder.ApiBuilder.post;

public class UserVoteRoute implements RoutesBuilder {
    private final VoteRepository voteRepository;
    private final Configuration configuration;

    public UserVoteRoute(VoteRepository voteRepository, Configuration configuration) {
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
                        content = {@OpenApiContent(from = TokensPOJO.class, type = "application/json")})
            })
    private void getUserTokens(@NotNull Context ctx) {
        UserSession session = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        int tokens = voteRepository.getUserToken(session.userId());
        ctx.json(new TokensPOJO(tokens));
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

    @OpenApi(
            summary = "Transfer user tokens to a guild",
            operationId = "transferTokensToGuild",
            path = "v1/user/vote/transfer",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "User Session Token")},
            tags = {"User"},
            requestBody =
                    @io.javalin.openapi.OpenApiRequestBody(
                            content = {@OpenApiContent(from = VoteTransferRequestPOJO.class)}),
            responses = {
                @OpenApiResponse(status = "204", description = "Transfer successful"),
                @OpenApiResponse(status = "400", description = "Invalid request or not enough tokens")
            })
    private void postTransfer(@NotNull Context ctx) {
        UserSession session = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        VoteTransferRequestPOJO body = ctx.bodyAsClass(VoteTransferRequestPOJO.class);
        if (body == null
                || body.amount() <= 0
                || body.guildId() == null
                || body.guildId().isBlank()) {
            ctx.status(400);
            return;
        }
        // Ensure the guild exists in the current user session
        if (session.guilds() == null || !session.guilds().containsKey(body.guildId())) {
            ctx.status(400);
            return;
        }
        long guildId;
        try {
            guildId = Long.parseLong(body.guildId());
        } catch (NumberFormatException e) {
            ctx.status(400);
            return;
        }
        boolean success = voteRepository.transferToGuild(session.userId(), guildId, body.amount());
        if (!success) {
            ctx.status(400);
            return;
        }
        ctx.status(204);
    }

    @Override
    public void buildRoutes() {
        path("vote", () -> {
            get("tokens", this::getUserTokens, Role.USER);
            get("lists", this::getUserVoteLists, Role.USER);
            get("log", this::getVoteLog, Role.USER);
            post("transfer", this::postTransfer, Role.USER);
        });
    }
}
