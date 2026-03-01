/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.guild;

import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.dao.provider.VoteRepository;
import de.chojo.repbot.service.TokenPurchaseService;
import de.chojo.repbot.web.cache.MemberCache;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.guild.DashboardStatsPOJO;
import de.chojo.repbot.web.pojo.guild.GuildPOJO;
import de.chojo.repbot.web.pojo.guild.RankingEntryPOJO;
import de.chojo.repbot.web.pojo.token.WithdrawRequestPOJO;
import de.chojo.repbot.web.pojo.user.TokensPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class GuildRoute implements RoutesBuilder {
    private final MemberCache memberCache;
    private final VoteRepository voteRepository;
    private final FeaturesRoute featuresRoute;

    public GuildRoute(
            MemberCache memberCache, VoteRepository voteRepository, TokenPurchaseService tokenPurchaseService) {
        this.memberCache = memberCache;
        this.voteRepository = voteRepository;
        featuresRoute = new FeaturesRoute(tokenPurchaseService);
    }

    @Override
    public void buildRoutes() {
        path("/guild", () -> {
            get("/stats", this::getStats, Role.GUILD_USER);
            get("/meta", this::getMeta, Role.GUILD_USER);
            path("/token", () -> {
                get(this::getTokens, Role.GUILD_USER);
                post("/withdraw", this::withdraw, Role.GUILD_ADMIN);
            });
            featuresRoute.buildRoutes();
        });
    }

    @OpenApi(
            summary = "Get guild tokens",
            operationId = "getGuildTokens",
            path = "v1/guild/token",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Guild"},
            responses = {@OpenApiResponse(status = "200", content = @OpenApiContent(from = TokensPOJO.class))})
    private void getTokens(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        ctx.json(new TokensPOJO(voteRepository.getGuildToken(session.guildId())));
    }

    @OpenApi(
            summary = "Withdraw guild tokens",
            operationId = "withdrawGuildTokens",
            path = "v1/guild/token/withdraw",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = WithdrawRequestPOJO.class)),
            tags = {"Guild"},
            responses = {@OpenApiResponse(status = "200", content = @OpenApiContent(from = TokensPOJO.class))})
    private void withdraw(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        WithdrawRequestPOJO request = ctx.bodyAsClass(WithdrawRequestPOJO.class);
        voteRepository.withdrawGuildTokens(session.guildId(), request.amount());
        ctx.json(new TokensPOJO(voteRepository.getGuildToken(session.guildId())));
    }

    @OpenApi(
            summary = "Get guild stats",
            operationId = "getGuildStats",
            path = "v1/guild/stats",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Guild"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = @io.javalin.openapi.OpenApiContent(from = DashboardStatsPOJO.class))
            })
    private void getStats(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        var reputation = session.repGuild().reputation();
        var stats = reputation.stats();
        var top = reputation.ranking().received().total(5).page(0).stream()
                .map(r -> RankingEntryPOJO.generate(r, memberCache.get(session.guild(), String.valueOf(r.userId()))))
                .toList();

        ctx.json(DashboardStatsPOJO.generate(stats, top));
    }

    @OpenApi(
            summary = "Get guild meta",
            operationId = "getGuildMeta",
            path = "v1/guild/meta",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Guild"},
            responses = {
                @OpenApiResponse(status = "200", content = @io.javalin.openapi.OpenApiContent(from = GuildPOJO.class))
            })
    private void getMeta(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        ctx.json(session.guildPOJO());
    }
}
