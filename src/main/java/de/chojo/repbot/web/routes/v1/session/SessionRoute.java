package de.chojo.repbot.web.routes.v1.session;

import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.GuildSessionPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.sessions.GuildSession;
import de.chojo.repbot.web.sessions.SessionService;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import org.jetbrains.annotations.NotNull;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class SessionRoute implements RoutesBuilder {
    private final SessionService sessionService;

    public SessionRoute(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @OpenApi(
            summary = "Get the data for a guild session.",
            operationId = "getGuildSession",
            path = "v1/session",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Session"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = GuildSessionPOJO.class, type = "application/json")})
            }
    )
    private static void getSessionData(@NotNull Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        ctx.json(session.sessionData());
    }

    @Override
    public void buildRoutes() {
        path("session", () -> {
            get(SessionRoute::getSessionData, Role.GUILD_USER);
        });
    }
}
