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

import java.util.function.BiConsumer;
import java.util.function.Predicate;

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
        MessagesPOJO oldValue = new MessagesPOJO(
                messages.isCommandReputationEphemeral(),
                messages.isAnnounceReaction(),
                messages.isAnnounceAnswer(),
                messages.isAnnounceMention(),
                messages.isAnnounceFuzzy(),
                messages.isAnnounceEmbed(),
                messages.isAnnounceDirect(),
                messages.isAnnounceCommand(),
                messages.isAnnounceDelete());
        MessagesPOJO messagesPOJO = ctx.bodyAsClass(MessagesPOJO.class);
        messages.apply(messagesPOJO);
        session.recordChange("messages", oldValue, messagesPOJO);
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
        update(
                ctx,
                "commandreputationephemeral",
                Messages::isCommandReputationEphemeral,
                Messages::commandReputationEphemeral);
    }

    @OpenApi(
            summary = "Update announcement of reputation given by reaction",
            operationId = "updateMessagesAnnounceReaction",
            path = "v1/settings/messages/announcereaction",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateAnnounceReaction(Context ctx) {
        update(ctx, "announcereaction", Messages::isAnnounceReaction, Messages::announceReaction);
    }

    @OpenApi(
            summary = "Update announcement of reputation given by answer",
            operationId = "updateMessagesAnnounceAnswer",
            path = "v1/settings/messages/announceanswer",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateAnnounceAnswer(Context ctx) {
        update(ctx, "announceanswer", Messages::isAnnounceAnswer, Messages::announceAnswer);
    }

    @OpenApi(
            summary = "Update announcement of reputation given by mention",
            operationId = "updateMessagesAnnounceMention",
            path = "v1/settings/messages/announcemention",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateAnnounceMention(Context ctx) {
        update(ctx, "announcemention", Messages::isAnnounceMention, Messages::announceMention);
    }

    @OpenApi(
            summary = "Update announcement of reputation given by fuzzy match",
            operationId = "updateMessagesAnnounceFuzzy",
            path = "v1/settings/messages/announcefuzzy",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateAnnounceFuzzy(Context ctx) {
        update(ctx, "announcefuzzy", Messages::isAnnounceFuzzy, Messages::announceFuzzy);
    }

    @OpenApi(
            summary = "Update announcement of reputation given by embed",
            operationId = "updateMessagesAnnounceEmbed",
            path = "v1/settings/messages/announceembed",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateAnnounceEmbed(Context ctx) {
        update(ctx, "announceembed", Messages::isAnnounceEmbed, Messages::announceEmbed);
    }

    @OpenApi(
            summary = "Update announcement of reputation given directly",
            operationId = "updateMessagesAnnounceDirect",
            path = "v1/settings/messages/announcedirect",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateAnnounceDirect(Context ctx) {
        update(ctx, "announcedirect", Messages::isAnnounceDirect, Messages::announceDirect);
    }

    @OpenApi(
            summary = "Update announcement of reputation given by command",
            operationId = "updateMessagesAnnounceCommand",
            path = "v1/settings/messages/announcecommand",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateAnnounceCommand(Context ctx) {
        update(ctx, "announcecommand", Messages::isAnnounceCommand, Messages::announceCommand);
    }

    @OpenApi(
            summary = "Update deletion of reputation announcements",
            operationId = "updateMessagesAnnounceDelete",
            path = "v1/settings/messages/announcedelete",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateAnnounceDelete(Context ctx) {
        update(ctx, "announcedelete", Messages::isAnnounceDelete, Messages::announceDelete);
    }

    private void update(Context ctx, String setting, Predicate<Messages> current, BiConsumer<Messages, Boolean> apply) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        Messages messages = session.repGuild().settings().messages();
        boolean oldValue = current.test(messages);
        boolean newValue = ctx.bodyAsClass(Boolean.class);
        apply.accept(messages, newValue);
        session.recordChange("messages.%s".formatted(setting), oldValue, newValue);
    }

    @Override
    public void buildRoutes() {
        path("messages", () -> {
            post("", this::updateMessagesSettings, Role.GUILD_ADMIN);
            post("commandreputationephemeral", this::updateCommandReputationEphemeral, Role.GUILD_ADMIN);
            post("announcereaction", this::updateAnnounceReaction, Role.GUILD_ADMIN);
            post("announceanswer", this::updateAnnounceAnswer, Role.GUILD_ADMIN);
            post("announcemention", this::updateAnnounceMention, Role.GUILD_ADMIN);
            post("announcefuzzy", this::updateAnnounceFuzzy, Role.GUILD_ADMIN);
            post("announceembed", this::updateAnnounceEmbed, Role.GUILD_ADMIN);
            post("announcedirect", this::updateAnnounceDirect, Role.GUILD_ADMIN);
            post("announcecommand", this::updateAnnounceCommand, Role.GUILD_ADMIN);
            post("announcedelete", this::updateAnnounceDelete, Role.GUILD_ADMIN);
        });
    }
}
