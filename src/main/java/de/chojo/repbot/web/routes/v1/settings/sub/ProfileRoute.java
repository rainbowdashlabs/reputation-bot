package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.Profile;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.error.ApiException;
import de.chojo.repbot.web.error.ErrorResponse;
import de.chojo.repbot.web.pojo.settings.sub.ProfilePOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.sessions.GuildSession;
import de.chojo.repbot.web.validation.PremiumValidator;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static io.javalin.apibuilder.ApiBuilder.delete;
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

        // Validate file size (max 2MB)
        int maxSize = 2 * 1024 * 1024;
        if (profilePicture.length > maxSize) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "File too large. Maximum size is 2MB.");
        }

        // Validate image dimensions (max 512x512)
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(profilePicture));
            if (image == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid image file.");
            }
            
            if (image.getWidth() > 512 || image.getHeight() > 512) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Image dimensions too large. Maximum size is 512x512 pixels.");
            }
        } catch (IOException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Failed to read image file.");
        }

        session.repGuild().settings().profile().profilePicture(profilePicture);
    }

    @OpenApi(
            summary = "Update reputation name",
            operationId = "updateReputationName",
            path = "v1/settings/profile/reputationname",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = String.class)),
            responses = {
                    @OpenApiResponse(status = "200"),
                    @OpenApiResponse(status = "403", content = @OpenApiContent(from = ErrorResponse.class), description = "Premium feature required or limit exceeded")
            }
    )
    public void updateReputationName(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        String reputationName = ctx.bodyAsClass(String.class);

        // Validate locale overrides feature if setting a reputation name
        PremiumValidator validator = session.premiumValidator();
        if (reputationName != null && !reputationName.isEmpty()) {
            validator.requireFeature(validator.features().localeOverrides(), "Locale Overrides");
        }

        session.repGuild().settings().profile().reputationName(reputationName);
    }


    @OpenApi(
            summary = "Delete bot nickname",
            operationId = "deleteBotNickname",
            path = "v1/settings/profile/nickname",
            methods = HttpMethod.DELETE,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            responses = {
                    @OpenApiResponse(status = "200")
            }
    )
    public void deleteNickname(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().profile().nickname(null);
    }

    @OpenApi(
            summary = "Delete bot profile picture",
            operationId = "deleteBotProfilePicture",
            path = "v1/settings/profile/picture",
            methods = HttpMethod.DELETE,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            responses = {
                    @OpenApiResponse(status = "200"),
                    @OpenApiResponse(status = "403", content = @OpenApiContent(from = ErrorResponse.class), description = "Premium feature required or limit exceeded")
            }
    )
    public void deleteProfilePicture(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        
        // Validate profile feature
        PremiumValidator validator = session.premiumValidator();
        validator.requireFeature(validator.features().profile(), "Profile");
        
        // Reset to default by setting null avatar
        session.repGuild().settings().profile().profilePicture(null);
    }

    @OpenApi(
            summary = "Delete reputation name",
            operationId = "deleteReputationName",
            path = "v1/settings/profile/reputationname",
            methods = HttpMethod.DELETE,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            responses = {
                    @OpenApiResponse(status = "200")
            }
    )
    public void deleteReputationName(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().profile().reputationName(null);
    }

    @Override
    public void buildRoutes() {
        path("profile", () -> {
            post("", this::updateProfileSettings, Role.GUILD_USER);
            post("nickname", this::updateNickname, Role.GUILD_USER);
            delete("nickname", this::deleteNickname, Role.GUILD_USER);
            post("picture", this::updateProfilePicture, Role.GUILD_USER);
            delete("picture", this::deleteProfilePicture, Role.GUILD_USER);
            post("reputationname", this::updateReputationName, Role.GUILD_USER);
            delete("reputationname", this::deleteReputationName, Role.GUILD_USER);
        });
    }
}
