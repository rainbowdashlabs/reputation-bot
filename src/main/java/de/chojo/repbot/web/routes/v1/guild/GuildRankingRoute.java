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

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class GuildRankingRoute extends BaseRankingRoute {

    public GuildRankingRoute(RankingService rankingService, Configuration configuration) {
        super(rankingService, configuration);
    }

    @Override
    public void buildRoutes() {
        path("/ranking", () -> {
            path("/given", () -> get(this::getGuildGiven, Role.GUILD_USER));
            path("/received", () -> get(this::getGuildReceived, Role.GUILD_USER));
        });
    }

    @OpenApi(
            summary = "Get guild given ranking",
            operationId = "getGuildGivenRanking",
            path = "v1/guild/ranking/given",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            queryParams = {
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
                @OpenApiResponse(status = "403", description = "Advanced rankings feature not unlocked")
            })
    private void getGuildGiven(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        if (!requireAdvancedRankings(ctx, session)) return;
        ReputationMode mode = resolveMode(ctx, session);
        boolean unlocked = session.premiumValidator().features().reputationLog().unlocked();
        if (unlocked) {
            ctx.json(rankingService.getGuildGiven(
                    session.guild(), mode, resolvePageSize(ctx, session), resolvePage(ctx, session)));
        } else {
            RankingPagePOJO guildReceived = rankingService.getGuildGiven(
                    session.guild(), mode, resolvePageSize(ctx, session), resolvePage(ctx, session));
            ctx.json(new RankingPagePOJO(
                    Math.min(configuration.skus().features().reputationLog().defaultSize(), guildReceived.pages()),
                    guildReceived.page(),
                    guildReceived.entries()));
        }
    }

    @OpenApi(
            summary = "Get guild received ranking",
            operationId = "getGuildReceivedRanking",
            path = "v1/guild/ranking/received",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            queryParams = {
                @OpenApiParam(name = "page", description = "Page number (0-based)", type = Integer.class),
                @OpenApiParam(
                        name = "pageSize",
                        description = "Page size (max 50, max 20 without reputationLog feature)",
                        type = Integer.class),
                @OpenApiParam(name = "mode", description = "Reputation mode")
            },
            tags = {"Guild"},
            responses = {@OpenApiResponse(status = "200", content = @OpenApiContent(from = RankingPagePOJO.class))})
    private void getGuildReceived(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        ReputationMode mode = resolveMode(ctx, session);
        boolean unlocked = session.premiumValidator().features().reputationLog().unlocked();
        if (unlocked) {
            ctx.json(rankingService.getGuildReceived(
                    session.guild(), mode, resolvePageSize(ctx, session), resolvePage(ctx, session)));
        } else {
            RankingPagePOJO guildReceived = rankingService.getGuildReceived(
                    session.guild(), mode, resolvePageSize(ctx, session), resolvePage(ctx, session));
            ctx.json(new RankingPagePOJO(
                    Math.min(configuration.skus().features().reputationLog().defaultSize(), guildReceived.pages()),
                    guildReceived.page(),
                    guildReceived.entries()));
        }
    }
}
