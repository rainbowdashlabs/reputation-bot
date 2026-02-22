/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.Messages;
import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.settings.sub.MessagesPOJO;
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

public class MessagesRoute implements RoutesBuilder {
    @OpenApi(
            summary = "Update messages settings",
            operationId = "updateMessagesSettings",
            path = "v1/settings/messages",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @io.javalin.openapi.OpenApiContent(from = MessagesPOJO.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateMessagesSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        Messages messages = session.repGuild().settings().messages();
        MessagesPOJO oldValue =
                new MessagesPOJO(messages.isReactionConfirmation(), messages.isCommandReputationEphemeral());
        MessagesPOJO messagesPOJO = ctx.bodyAsClass(MessagesPOJO.class);
        messages.apply(messagesPOJO);
        session.recordChange("messages", oldValue, messagesPOJO);
    }

    @OpenApi(
            summary = "Update messages reaction confirmation",
            operationId = "updateMessagesReactionConfirmation",
            path = "v1/settings/messages/reactionconfirmation",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateReactionConfirmation(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        Messages messages = session.repGuild().settings().messages();
        boolean oldValue = messages.isReactionConfirmation();
        boolean newValue = ctx.bodyAsClass(Boolean.class);
        messages.reactionConfirmation(newValue);
        session.recordChange("messages.reactionconfirmation", oldValue, newValue);
    }

    @OpenApi(
            summary = "Update messages command reputation ephemeral",
            operationId = "updateMessagesCommandReputationEphemeral",
            path = "v1/settings/messages/commandreputationephemeral",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateCommandReputationEphemeral(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        Messages messages = session.repGuild().settings().messages();
        boolean oldValue = messages.isCommandReputationEphemeral();
        boolean newValue = ctx.bodyAsClass(Boolean.class);
        messages.commandReputationEphemeral(newValue);
        session.recordChange("messages.commandreputationephemeral", oldValue, newValue);
    }

    @Override
    public void buildRoutes() {
        path("messages", () -> {
            post("", this::updateMessagesSettings, Role.GUILD_ADMIN);
            post("reactionconfirmation", this::updateReactionConfirmation, Role.GUILD_ADMIN);
            post("commandreputationephemeral", this::updateCommandReputationEphemeral, Role.GUILD_ADMIN);
        });
    }
}
