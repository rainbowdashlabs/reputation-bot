/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.service.CommandAliasService;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.settings.sub.CommandAliasPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.validation.PremiumValidator;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.FailedDependencyResponse;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import org.slf4j.Logger;

import java.util.Optional;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static org.slf4j.LoggerFactory.getLogger;

public class CommandAliasesRoute implements RoutesBuilder {
    private static final Logger log = getLogger(CommandAliasesRoute.class);
    private final CommandAliasService commandAliasService;

    public CommandAliasesRoute(CommandAliasService commandAliasService) {
        this.commandAliasService = commandAliasService;
    }

    @OpenApi(
            summary = "Set or reset the custom name of a command",
            operationId = "updateCommandAlias",
            path = "v1/settings/commandaliases",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = CommandAliasPOJO.class)),
            responses = {@OpenApiResponse(status = "200"), @OpenApiResponse(status = "400")})
    public void updateAlias(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        CommandAliasPOJO request = ctx.bodyAsClass(CommandAliasPOJO.class);

        PremiumValidator validator = session.premiumValidator();
        validator.requireFeature(validator.features().commandAliases(), "Command Aliases");

        var aliases = session.repGuild().settings().commandAliases();
        String oldValue = aliases.alias(request.command()).orElse(null);
        Optional<String> error;
        try {
            error = commandAliasService.setAlias(session.guild(), request.command(), request.alias());
        } catch (RuntimeException e) {
            log.error("Could not refresh guild commands", e);
            throw new FailedDependencyResponse("Could not refresh guild commands.");
        }
        error.ifPresent(message -> {
            throw new BadRequestResponse(message);
        });
        String newValue = aliases.alias(request.command()).orElse(null);
        session.recordChange("command_aliases." + request.command(), oldValue, newValue);
    }

    @Override
    public void buildRoutes() {
        path("commandaliases", () -> post("", this::updateAlias, Role.GUILD_ADMIN));
    }
}
