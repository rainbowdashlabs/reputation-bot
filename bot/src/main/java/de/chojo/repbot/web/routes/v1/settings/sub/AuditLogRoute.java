/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.dao.access.guildsession.SettingsAuditLog;
import de.chojo.repbot.dao.provider.SettingsAuditLogRepository;
import de.chojo.repbot.web.cache.MemberCache;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.guild.MemberPOJO;
import de.chojo.repbot.web.pojo.settings.sub.AuditLogPagePOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;

import java.util.List;
import java.util.stream.Collectors;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class AuditLogRoute implements RoutesBuilder {
    private final MemberCache memberCache;
    private final SettingsAuditLogRepository settingsAuditLogRepository;

    public AuditLogRoute(MemberCache memberCache, SettingsAuditLogRepository settingsAuditLogRepository) {
        this.memberCache = memberCache;
        this.settingsAuditLogRepository = settingsAuditLogRepository;
    }

    @OpenApi(
            summary = "Get audit log",
            operationId = "getAuditLog",
            path = "v1/settings/auditlog",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            queryParams = {
                @OpenApiParam(name = "page", description = "Page number (default: 0)"),
                @OpenApiParam(name = "entries", description = "Entries per page (default: 50)")
            },
            tags = {"Settings"},
            responses = {@OpenApiResponse(status = "200", content = @OpenApiContent(from = AuditLogPagePOJO.class))})
    public void getAuditLog(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
        int entries = ctx.queryParamAsClass("entries", Integer.class).getOrDefault(50);

        long guildId = session.repGuild().guildId();
        List<SettingsAuditLog> auditLogs = settingsAuditLogRepository.getAuditLog(guildId, page, entries);
        var members = auditLogs.stream()
                .map(SettingsAuditLog::memberId)
                .distinct()
                .map(e -> memberCache.get(session.guild(), String.valueOf(e)))
                .collect(Collectors.toMap(MemberPOJO::id, e -> e));
        long maxPages = settingsAuditLogRepository.getAuditLogPages(guildId, entries);

        ctx.json(new AuditLogPagePOJO(page, maxPages, auditLogs, members));
    }

    @Override
    public void buildRoutes() {
        path("auditlog", () -> {
            get("", this::getAuditLog, Role.GUILD_ADMIN);
        });
    }
}
