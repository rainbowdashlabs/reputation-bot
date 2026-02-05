/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.General;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.settings.sub.GeneralPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.time.Instant;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class GeneralRoute implements RoutesBuilder {
    @OpenApi(
            summary = "Update general settings",
            operationId = "updateGeneralSettings",
            path = "v1/settings/general",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @io.javalin.openapi.OpenApiContent(from = GeneralPOJO.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateGeneralSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        General general = session.repGuild().settings().general();
        GeneralPOJO oldValue = new GeneralPOJO(
                general.isStackRoles(),
                general.language().orElse(null),
                general.reputationMode(),
                general.resetDate(),
                general.systemChannel());
        GeneralPOJO generalPOJO = ctx.bodyAsClass(GeneralPOJO.class);
        general.apply(generalPOJO);
        session.recordChange("general", oldValue, generalPOJO);
    }

    @OpenApi(
            summary = "Update general language",
            operationId = "updateGeneralLanguage",
            path = "v1/settings/general/language",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = String.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateLanguage(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        String languageStr = ctx.bodyAsClass(String.class);
        // Convert internal name (e.g., "SPANISH") to DiscordLocale enum
        DiscordLocale locale =
                languageStr != null && !languageStr.isEmpty() ? DiscordLocale.valueOf(languageStr) : null;
        General general = session.repGuild().settings().general();
        DiscordLocale oldValue = general.language().orElse(null);
        general.language(locale);
        session.recordChange("general.language", oldValue, locale);
    }

    @OpenApi(
            summary = "Update general stack roles",
            operationId = "updateGeneralStackRoles",
            path = "v1/settings/general/stackroles",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateStackRoles(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        General general = session.repGuild().settings().general();
        boolean oldValue = general.isStackRoles();
        boolean newValue = ctx.bodyAsClass(Boolean.class);
        general.isStackRoles(newValue);
        session.recordChange("general.stackroles", oldValue, newValue);
    }

    @OpenApi(
            summary = "Update general reputation mode",
            operationId = "updateGeneralReputationMode",
            path = "v1/settings/general/reputationmode",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ReputationMode.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateReputationMode(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        General general = session.repGuild().settings().general();
        ReputationMode oldValue = general.reputationMode();
        ReputationMode newValue = ctx.bodyAsClass(ReputationMode.class);
        general.reputationMode(newValue);
        session.recordChange("general.reputationmode", oldValue, newValue);
    }

    @OpenApi(
            summary = "Update general system channel",
            operationId = "updateGeneralSystemChannel",
            path = "v1/settings/general/systemchannel",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Long.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateSystemChannel(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        Long channelId = ctx.bodyAsClass(Long.class);

        session.guildValidator().validateChannelIds(channelId);

        General general = session.repGuild().settings().general();
        long oldValue = general.systemChannel();
        general.systemChannel(channelId);
        session.recordChange("general.systemchannel", oldValue, channelId);
    }

    @OpenApi(
            summary = "Update general reset date",
            operationId = "updateGeneralResetDate",
            path = "v1/settings/general/resetdate",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Instant.class)),
            responses = {@OpenApiResponse(status = "200")})
    public void updateResetDate(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        General general = session.repGuild().settings().general();
        Instant oldValue = general.resetDate();
        Instant newValue = ctx.bodyAsClass(Instant.class);
        general.resetDate(newValue);
        session.recordChange("general.resetdate", oldValue, newValue);
    }

    @Override
    public void buildRoutes() {
        path("general", () -> {
            post("", this::updateGeneralSettings, Role.GUILD_USER);
            post("language", this::updateLanguage, Role.GUILD_USER);
            post("stackroles", this::updateStackRoles, Role.GUILD_USER);
            post("reputationmode", this::updateReputationMode, Role.GUILD_USER);
            post("systemchannel", this::updateSystemChannel, Role.GUILD_USER);
            post("resetdate", this::updateResetDate, Role.GUILD_USER);
        });
    }
}
