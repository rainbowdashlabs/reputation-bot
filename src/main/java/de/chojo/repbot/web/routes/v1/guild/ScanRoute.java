/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.guild;

import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.service.ScanService;
import de.chojo.repbot.service.scanservice.ScanProcess;
import de.chojo.repbot.service.scanservice.ScanResult;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.routes.RoutesBuilder;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;

import java.time.Instant;
import java.util.Optional;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class ScanRoute implements RoutesBuilder {
    private final ScanService scanService;

    public ScanRoute(ScanService scanService) {
        this.scanService = scanService;
    }

    @OpenApi(
            summary = "Start a new scan for the guild",
            operationId = "startScan",
            path = "v1/guild/scan/start",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Guild"},
            responses = {
                @OpenApiResponse(status = "202", description = "Scan started"),
                @OpenApiResponse(status = "409", description = "Scan already running")
            })
    public void startScan(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        if (scanService.getScanProcess(session.repGuild().guild()).isPresent()) {
            ctx.status(HttpStatus.CONFLICT).result("Scan already running");
            return;
        }
        scanService.scan(session.repGuild().guild());
        ctx.status(HttpStatus.ACCEPTED);
    }

    @OpenApi(
            summary = "Get the current scan status or the last scan result",
            operationId = "getScanStatus",
            path = "v1/guild/scan/status",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Guild"},
            responses = {
                @OpenApiResponse(status = "200", content = @io.javalin.openapi.OpenApiContent(from = ScanResult.class)),
                @OpenApiResponse(status = "404", description = "No scan result found")
            })
    public void getScanStatus(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        var process = scanService.getScanProcess(session.repGuild().guild());
        if (process.isPresent()) {
            ScanProcess scanProcess = process.get();
            ctx.json(new ScanResult(scanProcess.progress(), scanProcess.start(), null));
            return;
        }

        Optional<ScanResult> opt = session.repGuild().scan().getProgress();
        if (opt.isEmpty()) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }

        ScanResult scanResult = opt.get();
        if (scanResult.end() == null) {
            scanResult = new ScanResult(scanResult.progress(), scanResult.start(), Instant.now());
        }
        ctx.json(scanResult);
    }

    @Override
    public void buildRoutes() {
        path("scan", () -> {
            post("start", this::startScan, Role.GUILD_ADMIN);
            get("status", this::getScanStatus, Role.GUILD_ADMIN);
        });
    }
}
