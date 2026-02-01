package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.Profile;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.error.ErrorResponse;
import de.chojo.repbot.web.pojo.settings.sub.ProfilePOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.sessions.GuildSession;
import de.chojo.repbot.web.validation.PremiumValidator;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class ProfileRoute implements RoutesBuilder {
    @OpenApi(
            summary = "Update profile settings",
            operationId = "updateProfileSettings",
            path = "v1/settings/profile",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ProfilePOJO.class)),
            responses = {
                    @OpenApiResponse(status = "200"),
                    @OpenApiResponse(status = "403", content = @OpenApiContent(from = ErrorResponse.class), description = "Premium feature required or limit exceeded")
            }
    )
    public void updateProfileSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        ProfilePOJO profilePOJO = ctx.bodyAsClass(ProfilePOJO.class);

        // Validate profile feature if setting any profile field
        PremiumValidator validator = session.premiumValidator();
        boolean hasProfileData = (profilePOJO.nickname() != null && !profilePOJO.nickname().isEmpty());

        if (hasProfileData) {
            validator.requireFeature(validator.features().profile(), "Profile");
        }

        Profile profile = session.repGuild().settings().profile();
        profile.apply(profilePOJO);
    }

    @OpenApi(
            summary = "Update bot nickname",
            operationId = "updateBotNickname",
            path = "v1/settings/profile/nickname",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = String.class)),
            responses = {
                    @OpenApiResponse(status = "200"),
                    @OpenApiResponse(status = "403", content = @OpenApiContent(from = ErrorResponse.class), description = "Premium feature required or limit exceeded")
            }
    )
    public void updateNickname(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        String nickname = ctx.bodyAsClass(String.class);

        // Validate profile feature if setting a nickname
        PremiumValidator validator = session.premiumValidator();
        if (nickname != null && !nickname.isEmpty()) {
            validator.requireFeature(validator.features().profile(), "Profile");
        }

        session.repGuild().settings().profile().nickname(nickname);
    }

    @OpenApi(
            summary = "Update bot profile picture",
            operationId = "updateBotProfilePicture",
            path = "v1/settings/profile/picture",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = byte[].class, type = "image/*")),
            responses = {
                    @OpenApiResponse(status = "200"),
                    @OpenApiResponse(status = "403", content = @OpenApiContent(from = ErrorResponse.class), description = "Premium feature required or limit exceeded")
            }
    )
    public void updateProfilePicture(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        byte[] profilePicture = ctx.bodyAsBytes();

        // Validate profile feature
        PremiumValidator validator = session.premiumValidator();
        validator.requireFeature(validator.features().profile(), "Profile");

        session.repGuild().settings().profile().profilePicture(profilePicture);
    }


    @Override
    public void buildRoutes() {
        path("profile", () -> {
            post("", this::updateProfileSettings, Role.GUILD_USER);
            post("nickname", this::updateNickname, Role.GUILD_USER);
            post("picture", this::updateProfilePicture, Role.GUILD_USER);
        });
    }
}
