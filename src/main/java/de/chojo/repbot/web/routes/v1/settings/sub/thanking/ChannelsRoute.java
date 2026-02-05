/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.settings.sub.thanking;

import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.error.ErrorResponse;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ChannelsPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.dao.access.guildsession.GuildSession;
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

public class ChannelsRoute implements RoutesBuilder {
    @OpenApi(
            summary = "Update thanking channels settings",
            operationId = "updateThankingChannelsSettings",
            path = "v1/settings/thanking/channels",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ChannelsPOJO.class)),
            responses = {
                @OpenApiResponse(status = "200"),
                @OpenApiResponse(
                        status = "403",
                        content = @OpenApiContent(from = ErrorResponse.class),
                        description = "Premium feature required or limit exceeded")
            })
    public void updateChannelsSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        ChannelsPOJO channelsPOJO = ctx.bodyAsClass(ChannelsPOJO.class);

        // Validate all premium features in the combined POJO
        PremiumValidator validator = session.premiumValidator();

        // Validate channel count limit
        validator.requireWithinLimit(
                channelsPOJO.channelIds().size(), validator.features().reputationChannel(), "Reputation Channels");

        // Validate category count limit
        validator.requireWithinLimit(
                channelsPOJO.categoryIds().size(),
                validator.features().reputationCategories(),
                "Reputation Categories");

        // Validate whitelist mode (blacklist requires premium)
        validator.requireWhitelistOrPremium(channelsPOJO.isWhitelist());

        var channels = session.repGuild().settings().thanking().channels();
        var oldValue = new ChannelsPOJO(channels.channelIds(), channels.categoryIds(), channels.isWhitelist());
        channels.apply(channelsPOJO);
        session.recordChange("thanking.channels", oldValue, channelsPOJO);
    }

    @OpenApi(
            summary = "Update thanking channels whitelist",
            operationId = "updateThankingChannelsWhitelist",
            path = "v1/settings/thanking/channels/whitelist",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {
                @OpenApiResponse(status = "200"),
                @OpenApiResponse(
                        status = "403",
                        content = @OpenApiContent(from = ErrorResponse.class),
                        description = "Premium feature required or limit exceeded")
            })
    public void updateWhitelist(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        boolean isWhitelist = ctx.bodyAsClass(Boolean.class);

        // Validate that blacklist mode is allowed
        PremiumValidator validator = session.premiumValidator();
        validator.requireWhitelistOrPremium(isWhitelist);

        var channels = session.repGuild().settings().thanking().channels();
        boolean oldValue = channels.isWhitelist();
        channels.listType(isWhitelist);
        session.recordChange("thanking.channels.whitelist", oldValue, isWhitelist);
    }

    @OpenApi(
            summary = "Update thanking channels",
            operationId = "updateThankingChannelsList",
            path = "v1/settings/thanking/channels/channels",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Long[].class)),
            responses = {
                @OpenApiResponse(status = "200"),
                @OpenApiResponse(
                        status = "403",
                        content = @OpenApiContent(from = ErrorResponse.class),
                        description = "Premium feature required or limit exceeded")
            })
    public void updateChannels(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        Long[] channelIds = ctx.bodyAsClass(Long[].class);

        // Validate channel count limit
        PremiumValidator validator = session.premiumValidator();
        validator.requireWithinLimit(
                channelIds.length, validator.features().reputationChannel(), "Reputation Channels");

        // Validate all channel IDs
        for (Long channelId : channelIds) {
            session.guildValidator().validateChannelIds(channelId);
        }

        var channels = session.repGuild().settings().thanking().channels();
        var oldValue = channels.channelIds();
        channels.clearChannel();
        for (Long channelId : channelIds) {
            channels.addChannel(channelId);
        }
        var newValue = channels.channelIds();
        session.recordChange("thanking.channels.channels", oldValue, newValue);
    }

    @OpenApi(
            summary = "Update thanking categories",
            operationId = "updateThankingCategories",
            path = "v1/settings/thanking/channels/categories",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Long[].class)),
            responses = {
                @OpenApiResponse(status = "200"),
                @OpenApiResponse(
                        status = "403",
                        content = @OpenApiContent(from = ErrorResponse.class),
                        description = "Premium feature required or limit exceeded")
            })
    public void updateCategories(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        Long[] categoryIds = ctx.bodyAsClass(Long[].class);

        // Validate category count limit
        PremiumValidator validator = session.premiumValidator();
        validator.requireWithinLimit(
                categoryIds.length, validator.features().reputationCategories(), "Reputation Categories");

        // Validate all category IDs (categories are also channels in Discord)
        for (Long categoryId : categoryIds) {
            session.guildValidator().validateCategoryIds(categoryId);
        }

        var channels = session.repGuild().settings().thanking().channels();
        var oldValue = channels.categoryIds();
        channels.clearCategories();
        for (Long categoryId : categoryIds) {
            channels.addCategory(categoryId);
        }
        var newValue = channels.categoryIds();
        session.recordChange("thanking.channels.categories", oldValue, newValue);
    }

    @Override
    public void buildRoutes() {
        path("channels", () -> {
            post("", this::updateChannelsSettings, Role.GUILD_USER);
            post("whitelist", this::updateWhitelist, Role.GUILD_USER);
            post("channels", this::updateChannels, Role.GUILD_USER);
            post("categories", this::updateCategories, Role.GUILD_USER);
        });
    }
}
