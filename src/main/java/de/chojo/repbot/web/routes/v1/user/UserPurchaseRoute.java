/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.user;

import de.chojo.repbot.dao.access.user.RepUser;
import de.chojo.repbot.dao.access.user.sub.purchases.KofiPurchase;
import de.chojo.repbot.dao.provider.UserRepository;
import de.chojo.repbot.service.KofiService;
import de.chojo.repbot.service.kofi.SubscriptionResult;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.services.UserSession;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class UserPurchaseRoute implements RoutesBuilder {
    private final UserRepository userRepository;
    private final KofiService kofiService;
    private final ShardManager shardManager;

    public UserPurchaseRoute(UserRepository userRepository, KofiService kofiService, ShardManager shardManager) {
        this.userRepository = userRepository;
        this.kofiService = kofiService;
        this.shardManager = shardManager;
    }

    @OpenApi(
            summary = "Get current Ko-fi purchases for the authenticated user.",
            operationId = "getUserPurchases",
            path = "v1/user/purchases",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "User Session Token")},
            tags = {"User"},
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            content = {@OpenApiContent(from = KofiPurchase[].class, type = "application/json")})
            })
    private void getPurchases(@NotNull Context ctx) {
        UserSession session = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        RepUser user = userRepository.byId(session.userId());
        List<KofiPurchase> purchases = user.purchases().all();
        ctx.json(purchases);
    }

    public record AssignGuildPOJO(long guildId) {
    }

    @OpenApi(
            summary = "Assign a Ko-fi purchase to a guild.",
            operationId = "assignPurchaseToGuild",
            path = "v1/user/purchases/{purchaseId}/guild",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "User Session Token")},
            pathParams = {@OpenApiParam(name = "purchaseId", required = true, description = "ID of the purchase")},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = AssignGuildPOJO.class)),
            tags = {"User"},
            responses = {
                    @OpenApiResponse(status = "204"),
                    @OpenApiResponse(status = "400"),
                    @OpenApiResponse(status = "404")
            })
    private void assignPurchaseGuild(@NotNull Context ctx) {
        UserSession session = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        long purchaseId = Long.parseLong(ctx.pathParam("purchaseId"));
        AssignGuildPOJO body = ctx.bodyAsClass(AssignGuildPOJO.class);
        if (body.guildId() == 0 || !session.guilds().containsKey(String.valueOf(body.guildId()))) {
            throw new BadRequestResponse("Guild not in session");
        }
        Optional<KofiPurchase> kofiPurchases =
                userRepository.byId(session.userId()).purchases().byId(purchaseId);
        if (kofiPurchases.isEmpty()) throw new NotFoundResponse("Purchase not found");
        SubscriptionResult result =
                kofiService.enableSubscription(kofiPurchases.get(), shardManager.getGuildById(body.guildId()));
        if (result != SubscriptionResult.SUCCESS) {
            throw new BadRequestResponse(result.name());
        }
        ctx.status(204);
    }

    @OpenApi(
            summary = "Unassign a Ko-fi purchase from any guild.",
            operationId = "unassignPurchaseFromGuild",
            path = "v1/user/purchases/{purchaseId}/guild",
            methods = HttpMethod.DELETE,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "User Session Token")},
            pathParams = {@OpenApiParam(name = "purchaseId", required = true, description = "ID of the purchase")},
            tags = {"User"},
            responses = {@OpenApiResponse(status = "204"), @OpenApiResponse(status = "404")})
    private void unassignPurchaseGuild(@NotNull Context ctx) {
        UserSession session = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        long purchaseId = Long.parseLong(ctx.pathParam("purchaseId"));
        Optional<KofiPurchase> kofiPurchases =
                userRepository.byId(session.userId()).purchases().byId(purchaseId);
        if (kofiPurchases.isEmpty()) throw new NotFoundResponse("Purchase not found");
        boolean updated = kofiService.disableSubscription(kofiPurchases.get());
        if (!updated) {
            throw new NotFoundResponse("Purchase not found or not owned by user");
        }
        ctx.status(204);
    }

    @Override
    public void buildRoutes() {
        get(this::getPurchases, Role.USER);
        path(
                "{purchaseId}",
                () -> path("guild", () -> {
                    post(this::assignPurchaseGuild, Role.USER);
                    delete(this::unassignPurchaseGuild, Role.USER);
                }));
    }
}
