package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.error.ErrorResponse;
import de.chojo.repbot.web.pojo.settings.sub.ThankingPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.routes.v1.settings.sub.thanking.ChannelsRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.thanking.DonorRolesRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.thanking.ReactionsRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.thanking.ReceiverRolesRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.thanking.ThankwordsRoute;
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

public class ThankingRoute implements RoutesBuilder {
    private final ChannelsRoute channelsRoute = new ChannelsRoute();
    private final DonorRolesRoute donorRolesRoute = new DonorRolesRoute();
    private final ReceiverRolesRoute receiverRolesRoute = new ReceiverRolesRoute();
    private final ReactionsRoute reactionsRoute = new ReactionsRoute();
    private final ThankwordsRoute thankwordsRoute = new ThankwordsRoute();

    @OpenApi(
            summary = "Update thanking settings",
            operationId = "updateThankingSettings",
            path = "v1/settings/thanking",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ThankingPOJO.class)),
            responses = {
                    @OpenApiResponse(status = "200"),
                    @OpenApiResponse(status = "403", content = @OpenApiContent(from = ErrorResponse.class), description = "Premium feature required or limit exceeded")
            }
    )
    public void updateThankingSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        ThankingPOJO thankingPOJO = ctx.bodyAsClass(ThankingPOJO.class);

        // Validate premium features in the nested channels settings
        PremiumValidator validator = session.premiumValidator();
        if (thankingPOJO.channels() != null) {
            validator.requireWithinLimit(thankingPOJO.channels().channelIds().size(), validator.features().reputationChannel(), "Reputation Channels");
            validator.requireWithinLimit(thankingPOJO.channels().categoryIds().size(), validator.features().reputationCategories(), "Reputation Categories");
            validator.requireWhitelistOrPremium(thankingPOJO.channels().isWhitelist());
        }

        Thanking thanking = session.repGuild().settings().thanking();
        thanking.apply(thankingPOJO);
    }

    @Override
    public void buildRoutes() {
        path("thanking", () -> {
            post("", this::updateThankingSettings, Role.GUILD_USER);
            channelsRoute.buildRoutes();
            donorRolesRoute.buildRoutes();
            receiverRolesRoute.buildRoutes();
            reactionsRoute.buildRoutes();
            thankwordsRoute.buildRoutes();
        });
    }
}
