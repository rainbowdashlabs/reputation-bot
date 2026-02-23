/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.guild;

import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.service.TokenPurchaseService;
import de.chojo.repbot.util.EntityType;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.guild.features.ActiveFeaturePOJO;
import de.chojo.repbot.web.pojo.guild.features.FeaturePurchaseRequestPOJO;
import de.chojo.repbot.web.pojo.guild.features.FeaturePurchaseResultPOJO;
import de.chojo.repbot.web.pojo.guild.features.FeatureSubscriptionRequestPOJO;
import de.chojo.repbot.web.pojo.session.GuildSessionData;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.services.UserSession;
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

public class FeaturesRoute implements RoutesBuilder {
    private final TokenPurchaseService tokenPurchaseService;

    public FeaturesRoute(TokenPurchaseService tokenPurchaseService) {
        this.tokenPurchaseService = tokenPurchaseService;
    }

    @Override
    public void buildRoutes() {
        path("/features", () -> {
            get("/active", this::getActiveFeatures, Role.GUILD_USER);
            post("/purchase", this::purchaseFeature, Role.GUILD_USER);
            post("/subscribe", this::subscribeFeature, Role.GUILD_ADMIN);
            post("/unsubscribe", this::unsubscribeFeature, Role.GUILD_ADMIN);
        });
    }

    @OpenApi(
            summary = "Get active token features",
            operationId = "getActiveFeatures",
            path = "v1/guild/features/active",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Guild"},
            responses = {@OpenApiResponse(status = "200", content = @OpenApiContent(from = ActiveFeaturePOJO[].class))})
    private void getActiveFeatures(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        ctx.json(session.repGuild().subscriptions().tokenPurchases().stream()
                .map(ActiveFeaturePOJO::generate)
                .toList());
    }

    @OpenApi(
            summary = "Purchase token feature",
            operationId = "purchaseFeature",
            path = "v1/guild/features/purchase",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = FeaturePurchaseRequestPOJO.class)),
            tags = {"Guild"},
            responses = {
                @OpenApiResponse(status = "200", content = @OpenApiContent(from = FeaturePurchaseResultPOJO.class))
            })
    private void purchaseFeature(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        UserSession userSession = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        FeaturePurchaseRequestPOJO request = ctx.bodyAsClass(FeaturePurchaseRequestPOJO.class);

        long entityId;
        EntityType entityType;
        if (request.guildTokens()) {
            GuildSessionData guildSessionData = userSession.guilds().get(String.valueOf(session.guildId()));
            if (guildSessionData == null || guildSessionData.accessLevel() != Role.GUILD_ADMIN) {
                ctx.status(403);
                return;
            }
            entityId = session.guildId();
            entityType = EntityType.GUILD;
        } else {
            entityId = userSession.userId();
            entityType = EntityType.USER;
        }

        var result = tokenPurchaseService.purchaseFeature(request.featureId(), session.guildId(), entityId, entityType);
        ctx.json(FeaturePurchaseResultPOJO.generate(result));
    }

    @OpenApi(
            summary = "Subscribe token feature",
            operationId = "subscribeFeature",
            path = "v1/guild/features/subscribe",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = FeatureSubscriptionRequestPOJO.class)),
            tags = {"Guild"},
            responses = {
                @OpenApiResponse(status = "200", content = @OpenApiContent(from = FeaturePurchaseResultPOJO.class))
            })
    private void subscribeFeature(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        FeatureSubscriptionRequestPOJO request = ctx.bodyAsClass(FeatureSubscriptionRequestPOJO.class);

        var result = tokenPurchaseService.subscribeFeature(request.featureId(), session.guildId());
        ctx.json(FeaturePurchaseResultPOJO.generate(result));
    }

    @OpenApi(
            summary = "Unsubscribe token feature",
            operationId = "unsubscribeFeature",
            path = "v1/guild/features/unsubscribe",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = FeatureSubscriptionRequestPOJO.class)),
            tags = {"Guild"},
            responses = {
                @OpenApiResponse(status = "200", content = @OpenApiContent(from = FeaturePurchaseResultPOJO.class))
            })
    private void unsubscribeFeature(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        FeatureSubscriptionRequestPOJO request = ctx.bodyAsClass(FeatureSubscriptionRequestPOJO.class);

        var result = tokenPurchaseService.unsubscribeFeature(request.featureId(), session.guildId());
        ctx.json(FeaturePurchaseResultPOJO.generate(result));
    }
}
