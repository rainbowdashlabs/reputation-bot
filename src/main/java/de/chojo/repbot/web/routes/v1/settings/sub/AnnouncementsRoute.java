package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.Announcements;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.settings.sub.AnnouncementsPOJO;
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

public class AnnouncementsRoute implements RoutesBuilder {
    @OpenApi(
            summary = "Update announcements settings",
            operationId = "updateAnnouncementsSettings",
            path = "v1/settings/announcements",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = AnnouncementsPOJO.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateAnnouncementsSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        Announcements announcements = session.repGuild().settings().announcements();
        AnnouncementsPOJO announcementsPOJO = ctx.bodyAsClass(AnnouncementsPOJO.class);
        announcements.apply(announcementsPOJO);
    }

    @OpenApi(
            summary = "Update announcements active",
            operationId = "updateAnnouncementsActive",
            path = "v1/settings/announcements/active",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateActive(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().announcements().active(ctx.bodyAsClass(Boolean.class));
    }

    @OpenApi(
            summary = "Update announcements same channel",
            operationId = "updateAnnouncementsSameChannel",
            path = "v1/settings/announcements/samechannel",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateSameChannel(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().announcements().sameChannel(ctx.bodyAsClass(Boolean.class));
    }

    @OpenApi(
            summary = "Update announcements channel",
            operationId = "updateAnnouncementsChannel",
            path = "v1/settings/announcements/channel",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Long.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateChannel(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().announcements().channel(ctx.bodyAsClass(Long.class));
    }

    @Override
    public void buildRoutes() {
        path("announcements", () -> {
            post("", this::updateAnnouncementsSettings, Role.GUILD_USER);
            post("active", this::updateActive, Role.GUILD_USER);
            post("samechannel", this::updateSameChannel, Role.GUILD_USER);
            post("channel", this::updateChannel, Role.GUILD_USER);
        });
    }
}
