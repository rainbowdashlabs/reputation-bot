package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.General;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.settings.sub.GeneralPOJO;
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

public class GeneralRoute implements RoutesBuilder {
    @OpenApi(
            summary = "Update general settings",
            operationId = "updateGeneralSettings",
            path = "v1/settings/general",
            methods = HttpMethod.POST,
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @io.javalin.openapi.OpenApiContent(from = GeneralPOJO.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateGeneralSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        General general = session.repGuild().settings().general();
        GeneralPOJO generalPOJO = ctx.bodyAsClass(GeneralPOJO.class);
        general.apply(generalPOJO);
    }

    @OpenApi(
            summary = "Update general language",
            operationId = "updateGeneralLanguage",
            path = "v1/settings/general/language",
            methods = HttpMethod.POST,
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = net.dv8tion.jda.api.interactions.DiscordLocale.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateLanguage(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().general().language(ctx.bodyAsClass(net.dv8tion.jda.api.interactions.DiscordLocale.class));
    }

    @OpenApi(
            summary = "Update general stack roles",
            operationId = "updateGeneralStackRoles",
            path = "v1/settings/general/stackroles",
            methods = HttpMethod.POST,
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateStackRoles(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().general().isStackRoles(ctx.bodyAsClass(Boolean.class));
    }

    @OpenApi(
            summary = "Update general reputation mode",
            operationId = "updateGeneralReputationMode",
            path = "v1/settings/general/reputationmode",
            methods = HttpMethod.POST,
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateReputationMode(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().general().reputationMode(ctx.bodyAsClass(de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode.class));
    }

    @OpenApi(
            summary = "Update general system channel",
            operationId = "updateGeneralSystemChannel",
            path = "v1/settings/general/systemchannel",
            methods = HttpMethod.POST,
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Long.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateSystemChannel(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().general().systemChannel(ctx.bodyAsClass(Long.class));
    }

    @Override
    public void buildRoutes() {
        path("general", () -> {
            post("", this::updateGeneralSettings, Role.GUILD_USER);
            post("language", this::updateLanguage, Role.GUILD_USER);
            post("stackroles", this::updateStackRoles, Role.GUILD_USER);
            post("reputationmode", this::updateReputationMode, Role.GUILD_USER);
            post("systemchannel", this::updateSystemChannel, Role.GUILD_USER);
        });
    }
}
