package de.chojo.repbot.web.routes.v1.settings.sub.thanking;

import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ReactionsPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.sessions.GuildSession;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

import java.util.Set;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static java.util.Arrays.*;

public class ReactionsRoute implements RoutesBuilder {
    @OpenApi(
            summary = "Update thanking reactions settings",
            operationId = "updateThankingReactionsSettings",
            path = "v1/settings/thanking/reactions",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ReactionsPOJO.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateReactionsSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().thanking().reactions().apply(ctx.bodyAsClass(ReactionsPOJO.class));
    }

    @OpenApi(
            summary = "Update thanking main reaction",
            operationId = "updateThankingMainReaction",
            path = "v1/settings/thanking/reactions/mainreaction",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = String.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateMainReaction(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().thanking().reactions().mainReaction(ctx.bodyAsClass(String.class));
    }

    @OpenApi(
            summary = "Update thanking additional reactions",
            operationId = "updateThankingAdditionalReactions",
            path = "v1/settings/thanking/reactions/reactions",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = String[].class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateAdditionalReactions(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        var reactions = session.repGuild().settings().thanking().reactions();
        reactions.apply(new ReactionsPOJO(new java.util.HashSet<>(asList(ctx.bodyAsClass(String[].class))), reactions.mainReaction()));
    }

    @Override
    public void buildRoutes() {
        path("reactions", () -> {
            post("", this::updateReactionsSettings, Role.GUILD_USER);
            post("mainreaction", this::updateMainReaction, Role.GUILD_USER);
            post("reactions", this::updateAdditionalReactions, Role.GUILD_USER);
        });
    }
}
