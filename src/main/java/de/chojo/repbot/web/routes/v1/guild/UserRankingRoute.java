/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.guild;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.ranking.RankingPagePOJO;
import de.chojo.repbot.web.services.RankingService;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import net.dv8tion.jda.api.entities.Member;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class UserRankingRoute extends BaseRankingRoute {

    public UserRankingRoute(RankingService rankingService, Configuration configuration) {
        super(rankingService, configuration);
    }

    @Override
    public void buildRoutes() {
        path("/ranking", () -> {
            path("/given", () -> get(this::getUserGiven, Role.GUILD_USER));
            path("/received", () -> get(this::getUserReceived, Role.GUILD_USER));
        });
    }

    @OpenApi(
            summary = "Get user given ranking",
            operationId = "getUserGivenRanking",
            path = "v1/guild/user/ranking/given",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            queryParams = {
                @OpenApiParam(name = "userId", description = "User ID (defaults to authenticated user)"),
                @OpenApiParam(name = "page", description = "Page number (0-based)", type = Integer.class),
                @OpenApiParam(
                        name = "pageSize",
                        description = "Page size (max 50, max 20 without reputationLog feature)",
                        type = Integer.class),
                @OpenApiParam(name = "mode", description = "Reputation mode")
            },
            tags = {"Guild"},
            responses = {
                @OpenApiResponse(status = "200", content = @OpenApiContent(from = RankingPagePOJO.class)),
                @OpenApiResponse(status = "400", description = "Invalid user ID"),
                @OpenApiResponse(status = "403", description = "Advanced rankings feature not unlocked"),
                @OpenApiResponse(status = "404", description = "User not found in guild")
            })
    private void getUserGiven(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        if (!requireAdvancedRankings(ctx, session)) return;
        var member = resolveMember(ctx, session);
        if (member == null) return;
        ReputationMode mode = resolveMode(ctx, session);
        ctx.json(rankingService.getUserGiven(
                session.guild(), member, mode, resolvePageSize(ctx, session), resolvePage(ctx, session)));
    }

    @OpenApi(
            summary = "Get user received ranking",
            operationId = "getUserReceivedRanking",
            path = "v1/guild/user/ranking/received",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            queryParams = {
                @OpenApiParam(name = "userId", description = "User ID (defaults to authenticated user)"),
                @OpenApiParam(name = "page", description = "Page number (0-based)", type = Integer.class),
                @OpenApiParam(
                        name = "pageSize",
                        description = "Page size (max 50, max 20 without reputationLog feature)",
                        type = Integer.class),
                @OpenApiParam(name = "mode", description = "Reputation mode")
            },
            tags = {"Guild"},
            responses = {
                @OpenApiResponse(status = "200", content = @OpenApiContent(from = RankingPagePOJO.class)),
                @OpenApiResponse(status = "400", description = "Invalid user ID"),
                @OpenApiResponse(status = "403", description = "Advanced rankings feature not unlocked"),
                @OpenApiResponse(status = "404", description = "User not found in guild")
            })
    private void getUserReceived(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        if (!requireAdvancedRankings(ctx, session)) return;
        var member = resolveMember(ctx, session);
        if (member == null) return;
        ReputationMode mode = resolveMode(ctx, session);
        ctx.json(rankingService.getUserReceived(
                session.guild(), member, mode, resolvePageSize(ctx, session), resolvePage(ctx, session)));
    }

    private Member resolveMember(Context ctx, GuildSession session) {
        String userIdParam = ctx.queryParam("userId");
        Member member = null;
        if (userIdParam == null) {
            try {
                member = session.guild().retrieveMemberById(session.userId()).complete();
            } catch (Exception ignore) {
            }
            if (member == null) {
                ctx.status(404);
                return null;
            }
            return member;
        }
        long targetUserId;
        try {
            targetUserId = Long.parseLong(userIdParam);
        } catch (NumberFormatException e) {
            ctx.status(400);
            return null;
        }
        try {
            member = session.guild().retrieveMemberById(targetUserId).complete();
        } catch (Exception ignore) {
        }
        if (member == null) {
            ctx.status(404);
            return null;
        }
        return member;
    }
}
