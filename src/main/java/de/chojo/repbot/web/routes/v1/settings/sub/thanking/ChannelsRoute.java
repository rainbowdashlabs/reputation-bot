package de.chojo.repbot.web.routes.v1.settings.sub.thanking;

import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ChannelsPOJO;
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

public class ChannelsRoute implements RoutesBuilder {
    @OpenApi(
            summary = "Update thanking channels settings",
            operationId = "updateThankingChannelsSettings",
            path = "v1/settings/thanking/channels",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ChannelsPOJO.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateChannelsSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().thanking().channels().apply(ctx.bodyAsClass(ChannelsPOJO.class));
    }

    @OpenApi(
            summary = "Update thanking channels whitelist",
            operationId = "updateThankingChannelsWhitelist",
            path = "v1/settings/thanking/channels/whitelist",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateWhitelist(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().thanking().channels().listType(ctx.bodyAsClass(Boolean.class));
    }

    @OpenApi(
            summary = "Update thanking channels",
            operationId = "updateThankingChannelsList",
            path = "v1/settings/thanking/channels/channels",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Long[].class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateChannels(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        var channels = session.repGuild().settings().thanking().channels();
        channels.clearChannel();
        for (Long channelId : ctx.bodyAsClass(Long[].class)) {
            channels.addChannel(channelId);
        }
    }

    @OpenApi(
            summary = "Update thanking categories",
            operationId = "updateThankingCategories",
            path = "v1/settings/thanking/channels/categories",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Long[].class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateCategories(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        var channels = session.repGuild().settings().thanking().channels();
        channels.clearCategories();
        for (Long categoryId : ctx.bodyAsClass(Long[].class)) {
            channels.addCategory(categoryId);
        }
    }

    @Override
    public void buildRoutes() {
        path("channels", () -> {
            post("", this::updateChannelsSettings, Role.GUILD_USER);
            post("whitelist", this::updateWhitelist, Role.GUILD_USER);
            post("channels", this::updateChannels, Role.GUILD_USER);
            post("categories", this::updateCategories, Role.GUILD_USER);
        });
    }
}
