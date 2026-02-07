/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.error.ErrorResponseWrapper;
import de.chojo.repbot.web.pojo.settings.sub.ThankingPOJO;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ChannelsPOJO;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ReactionsPOJO;
import de.chojo.repbot.web.pojo.settings.sub.thanking.RolesHolderPOJO;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ThankwordsPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.routes.v1.settings.sub.thanking.ChannelsRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.thanking.DenyDonorRolesRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.thanking.DenyReceiverRolesRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.thanking.DonorRolesRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.thanking.ReactionsRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.thanking.ReceiverRolesRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.thanking.ThankwordsRoute;
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
    private final DenyDonorRolesRoute denyDonorRolesRoute = new DenyDonorRolesRoute();
    private final ReceiverRolesRoute receiverRolesRoute = new ReceiverRolesRoute();
    private final DenyReceiverRolesRoute denyReceiverRolesRoute = new DenyReceiverRolesRoute();
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
                @OpenApiResponse(
                        status = "403",
                        content = @OpenApiContent(from = ErrorResponseWrapper.class),
                        description = "Premium feature required or limit exceeded")
            })
    public void updateThankingSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        ThankingPOJO thankingPOJO = ctx.bodyAsClass(ThankingPOJO.class);

        // Validate premium features in the nested channels settings
        PremiumValidator validator = session.premiumValidator();
        if (thankingPOJO.channels() != null) {
            validator.requireWithinLimit(
                    thankingPOJO.channels().channelIds().size(),
                    validator.features().reputationChannel(),
                    "Reputation Channels");
            validator.requireWithinLimit(
                    thankingPOJO.channels().categoryIds().size(),
                    validator.features().reputationCategories(),
                    "Reputation Categories");
            validator.requireWhitelistOrPremium(thankingPOJO.channels().isWhitelist());
        }

        Thanking thanking = session.repGuild().settings().thanking();
        var oldValue = new ThankingPOJO(
                new ChannelsPOJO(
                        thanking.channels().copyChannelIds(),
                        thanking.channels().copyCategoryIds(),
                        thanking.channels().isWhitelist()),
                new RolesHolderPOJO(thanking.donorRoles().copyRoleIds()),
                new RolesHolderPOJO(thanking.denyDonorRoles().copyRoleIds()),
                new RolesHolderPOJO(thanking.receiverRoles().copyRoleIds()),
                new RolesHolderPOJO(thanking.denyReceiverRoles().copyRoleIds()),
                new ReactionsPOJO(
                        thanking.reactions().reactions(), thanking.reactions().mainReaction()),
                new ThankwordsPOJO(thanking.thankwords().words()));
        thanking.apply(thankingPOJO);
        session.recordChange("thanking", oldValue, thankingPOJO);
    }

    @Override
    public void buildRoutes() {
        path("thanking", () -> {
            post("", this::updateThankingSettings, Role.GUILD_USER);
            channelsRoute.buildRoutes();
            donorRolesRoute.buildRoutes();
            denyDonorRolesRoute.buildRoutes();
            receiverRolesRoute.buildRoutes();
            denyReceiverRolesRoute.buildRoutes();
            reactionsRoute.buildRoutes();
            thankwordsRoute.buildRoutes();
        });
    }
}
