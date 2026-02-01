package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.LogChannel;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.settings.sub.LogChannelPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.sessions.GuildSession;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class LogChannelRoute implements RoutesBuilder {
    @OpenApi(
            summary = "Update log channel settings",
            operationId = "updateLogChannelSettings",
            path = "v1/settings/logchannel",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @io.javalin.openapi.OpenApiContent(from = LogChannelPOJO.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateLogChannelSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        LogChannel logChannel = session.repGuild().settings().logChannel();
        LogChannelPOJO logChannelPOJO = ctx.bodyAsClass(LogChannelPOJO.class);
        logChannel.apply(logChannelPOJO);
    }

    @OpenApi(
            summary = "Update log channel active",
            operationId = "updateLogChannelActive",
            path = "v1/settings/logchannel/active",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateActive(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().logChannel().active(ctx.bodyAsClass(Boolean.class));
    }

    @OpenApi(
            summary = "Update log channel id",
            operationId = "updateLogChannelId",
            path = "v1/settings/logchannel/channel",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Long.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateChannel(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().logChannel().channel(ctx.bodyAsClass(Long.class));
    }

    @Override
    public void buildRoutes() {
        path("logchannel", () -> {
            post("", this::updateLogChannelSettings, Role.GUILD_USER);
            post("active", this::updateActive, Role.GUILD_USER);
            post("channel", this::updateChannel, Role.GUILD_USER);
        });
    }
}
