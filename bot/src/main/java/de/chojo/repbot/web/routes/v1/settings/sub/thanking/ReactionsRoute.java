/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.settings.sub.thanking;

import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ReactionsPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static java.util.Arrays.asList;

public class ReactionsRoute implements RoutesBuilder {
    @OpenApi(
            summary = "Update thanking reactions settings",
            operationId = "updateThankingReactionsSettings",
            path = "v1/settings/thanking/reactions",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ReactionsPOJO.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateReactionsSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        var reactions = session.repGuild().settings().thanking().reactions();
        var oldValue = new ReactionsPOJO(reactions.copyReactions(), reactions.mainReaction());
        ReactionsPOJO newValue = ctx.bodyAsClass(ReactionsPOJO.class);
        reactions.apply(newValue);
        session.recordChange("thanking.reactions", oldValue, newValue);
    }

    @OpenApi(
            summary = "Update thanking main reaction",
            operationId = "updateThankingMainReaction",
            path = "v1/settings/thanking/reactions/mainreaction",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = String.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateMainReaction(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        var reactions = session.repGuild().settings().thanking().reactions();
        String oldValue = reactions.mainReaction();
        String newValue = ctx.bodyAsClass(String.class);
        reactions.mainReaction(newValue);
        session.recordChange("thanking.reactions.mainreaction", oldValue, newValue);
    }

    @OpenApi(
            summary = "Update thanking additional reactions",
            operationId = "updateThankingAdditionalReactions",
            path = "v1/settings/thanking/reactions/reactions",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = String[].class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateAdditionalReactions(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        var reactions = session.repGuild().settings().thanking().reactions();
        var oldValue = reactions.copyReactions();
        ReactionsPOJO newValue = new ReactionsPOJO(
                new java.util.HashSet<>(asList(ctx.bodyAsClass(String[].class))), reactions.mainReaction());
        reactions.apply(newValue);
        session.recordChange("thanking.reactions.reactions", oldValue, newValue.reactions());
    }

    @Override
    public void buildRoutes() {
        path("reactions", () -> {
            post("", this::updateReactionsSettings, Role.GUILD_ADMIN);
            post("mainreaction", this::updateMainReaction, Role.GUILD_ADMIN);
            post("reactions", this::updateAdditionalReactions, Role.GUILD_ADMIN);
        });
    }
}
