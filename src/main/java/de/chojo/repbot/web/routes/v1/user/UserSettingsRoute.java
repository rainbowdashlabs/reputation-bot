/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.user;

import de.chojo.repbot.dao.access.user.sub.MailEntry;
import de.chojo.repbot.dao.access.user.sub.MailSource;
import de.chojo.repbot.dao.access.user.sub.UserSettings;
import de.chojo.repbot.dao.provider.UserRepository;
import de.chojo.repbot.service.MailService;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.user.UserSettingsPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.services.UserSession;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.patch;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class UserSettingsRoute implements RoutesBuilder {
    private final UserRepository userRepository;
    private final de.chojo.repbot.service.MailService mailService;
    private final ShardManager shardManager;

    public UserSettingsRoute(UserRepository userRepository, ShardManager shardManager, MailService mailService) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.shardManager = shardManager;
    }

    @Override
    public void buildRoutes() {
        get(this::getUserSettings, Role.USER);
        patch(this::updateUserSettings, Role.USER);
        path("mail", () -> {
            get(this::getUserMails, Role.USER);
            post(this::registerMail, Role.USER);
            path("{hash}", () -> {
                delete(this::unregisterMail, Role.USER);
                path("verify", () -> post(this::verifyMail, Role.USER));
            });
        });
    }

    @OpenApi(
            summary = "Get the current user settings.",
            operationId = "getUserSettings",
            path = "v1/user/settings",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "User Session Token")},
            tags = {"User"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = UserSettingsPOJO.class, type = "application/json")})
            })
    private void getUserSettings(@NotNull Context ctx) {
        UserSession session = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        UserSettings settings = userRepository.byId(session.userId()).settings();
        ctx.json(new UserSettingsPOJO(settings.voteGuild()));
    }

    @OpenApi(
            summary = "Update the user settings.",
            operationId = "updateUserSettings",
            path = "v1/user/settings",
            methods = HttpMethod.PATCH,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "User Session Token")},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = UserSettingsPOJO.class)),
            tags = {"User"},
            responses = {@OpenApiResponse(status = "204")})
    private void updateUserSettings(@NotNull Context ctx) {
        UserSession session = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        UserSettingsPOJO body = ctx.bodyAsClass(UserSettingsPOJO.class);

        if (body.voteGuild() != 0 && !session.guilds().containsKey(String.valueOf(body.voteGuild()))) {
            ctx.status(400).result("Guild not in session");
            return;
        }

        UserSettings settings = userRepository.byId(session.userId()).settings();
        settings.voteGuild(body.voteGuild());
        ctx.status(204);
    }

    @OpenApi(
            summary = "Register a new mail address for the user and send a verification mail.",
            operationId = "registerUserMail",
            path = "v1/user/settings/mail",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "User Session Token")},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = RegisterMailPOJO.class)),
            tags = {"User"},
            responses = {@OpenApiResponse(status = "200"), @OpenApiResponse(status = "400")})
    private void registerMail(@NotNull Context ctx) {
        UserSession session = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        RegisterMailPOJO body = ctx.bodyAsClass(RegisterMailPOJO.class);
        var result = mailService.registerAndPromptVerify(session.userId(), body.mail(), MailSource.USER);
        if (result.isFailure()) {
            ctx.status(400).result(result.failureReason().name());
            return;
        }
        ctx.json(result.result());
    }

    @OpenApi(
            summary = "Unregister an existing mail address by its hash.",
            operationId = "unregisterUserMail",
            path = "v1/user/settings/mail/{hash}",
            methods = HttpMethod.DELETE,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "User Session Token")},
            pathParams = {@OpenApiParam(name = "hash", required = true)},
            tags = {"User"},
            responses = {@OpenApiResponse(status = "204"), @OpenApiResponse(status = "404")})
    private void unregisterMail(@NotNull Context ctx) {
        // Only mails with source USER may be removed
        UserSession session = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        String hash = ctx.pathParam("hash");
        var userMails = userRepository.byId(session.userId()).mails();
        var optMail = userMails.getMail(hash);
        if (optMail.isEmpty()) {
            ctx.status(404).result("Mail not found");
            return;
        }
        MailEntry mail = optMail.get();
        if (mail.source() != MailSource.USER) {
            ctx.status(400).result("Mail cannot be removed (not user source)");
            return;
        }
        boolean removed = userMails.removeMail(hash);
        if (!removed) {
            ctx.status(404).result("Mail not found");
            return;
        }
        ctx.status(204);
    }

    @OpenApi(
            summary = "Verify a mail address using its hash and a verification code.",
            operationId = "verifyUserMail",
            path = "v1/user/settings/mail/{hash}/verify",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "User Session Token")},
            pathParams = {@OpenApiParam(name = "hash", required = true)},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = VerifyMailPOJO.class)),
            tags = {"User"},
            responses = {@OpenApiResponse(status = "204"), @OpenApiResponse(status = "400")})
    private void verifyMail(@NotNull Context ctx) {
        UserSession session = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        String hash = ctx.pathParam("hash");
        VerifyMailPOJO body = ctx.bodyAsClass(VerifyMailPOJO.class);
        var failure = mailService.verifyMail(session.userId(), hash, body.code());
        if (failure != null) {
            ctx.status(400).result(failure.name());
            return;
        }
        ctx.status(204);
    }

    @OpenApi(
            summary = "Get all mail addresses of the current user.",
            operationId = "getUserMails",
            path = "v1/user/settings/mail",
            methods = HttpMethod.GET,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "User Session Token")},
            tags = {"User"},
            responses = {
                @OpenApiResponse(
                        status = "200",
                        content = {@OpenApiContent(from = MailEntry[].class, type = "application/json")})
            })
    private void getUserMails(@NotNull Context ctx) {
        UserSession session = ctx.sessionAttribute(SessionAttribute.USER_SESSION);
        Collection<MailEntry> mails =
                userRepository.byId(session.userId()).mails().mails().values();
        ctx.json(mails);
    }

    public record RegisterMailPOJO(String mail) {}

    public record VerifyMailPOJO(String code) {}
}
