package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.autopost.Autopost;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.settings.sub.AutopostPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.sessions.GuildSession;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class AutopostRoute implements RoutesBuilder {
    @OpenApi(
            summary = "Update autopost settings",
            operationId = "updateAutopostSettings",
            path = "v1/settings/autopost",
            methods = HttpMethod.POST,
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @io.javalin.openapi.OpenApiContent(from = AutopostPOJO.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateAutopostSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        Autopost autopost = session.repGuild().settings().autopost();
        AutopostPOJO autopostPOJO = ctx.bodyAsClass(AutopostPOJO.class);
        autopost.apply(autopostPOJO);
    }

    @OpenApi(
            summary = "Update autopost active",
            operationId = "updateAutopostActive",
            path = "v1/settings/autopost/active",
            methods = HttpMethod.POST,
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateActive(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().autopost().active(ctx.bodyAsClass(Boolean.class));
    }

    @OpenApi(
            summary = "Update autopost channel",
            operationId = "updateAutopostChannel",
            path = "v1/settings/autopost/channel",
            methods = HttpMethod.POST,
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Long.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateChannel(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().autopost().channel(ctx.bodyAsClass(Long.class));
    }

    @OpenApi(
            summary = "Update autopost message",
            operationId = "updateAutopostMessage",
            path = "v1/settings/autopost/message",
            methods = HttpMethod.POST,
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Long.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateMessage(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().autopost().message(ctx.bodyAsClass(Long.class));
    }

    @OpenApi(
            summary = "Update autopost refresh type",
            operationId = "updateAutopostRefreshType",
            path = "v1/settings/autopost/refreshtype",
            methods = HttpMethod.POST,
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshType.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateRefreshType(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().autopost().refreshType(ctx.bodyAsClass(de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshType.class));
    }

    @OpenApi(
            summary = "Update autopost refresh interval",
            operationId = "updateAutopostRefreshInterval",
            path = "v1/settings/autopost/refreshinterval",
            methods = HttpMethod.POST,
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshInterval.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateRefreshInterval(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().autopost().refreshInterval(ctx.bodyAsClass(de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshInterval.class));
    }

    @Override
    public void buildRoutes() {
        path("autopost", () -> {
            post("", this::updateAutopostSettings, Role.GUILD_USER);
            post("active", this::updateActive, Role.GUILD_USER);
            post("channel", this::updateChannel, Role.GUILD_USER);
            post("message", this::updateMessage, Role.GUILD_USER);
            post("refreshtype", this::updateRefreshType, Role.GUILD_USER);
            post("refreshinterval", this::updateRefreshInterval, Role.GUILD_USER);
        });
    }
}
