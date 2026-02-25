/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.data;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.config.elements.Links;
import de.chojo.repbot.config.elements.sku.SKUFeatures;
import de.chojo.repbot.core.Localization;
import de.chojo.repbot.serialization.ThankwordsContainer;
import de.chojo.repbot.service.MarkdownService;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.pojo.general.LanguageInfo;
import de.chojo.repbot.web.routes.RoutesBuilder;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import net.dv8tion.jda.api.entities.SKU;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class DataRoute implements RoutesBuilder {
    private final ThankwordsContainer thankwordsContainer;
    private final Localization localization;
    private final Configuration configuration;
    private final ShardManager shardManager;
    private final MarkdownService markdownService;
    private final List<SKU> skus;

    public DataRoute(
            ThankwordsContainer thankwordsContainer,
            Localization localization,
            Configuration configuration,
            ShardManager shardManager,
            MarkdownService markdownService) {
        this.thankwordsContainer = thankwordsContainer;
        this.localization = localization;
        this.configuration = configuration;
        this.shardManager = shardManager;
        this.markdownService = markdownService;
        this.skus = shardManager.getShards().getFirst().retrieveSKUList().complete();
    }

    @OpenApi(
            summary = "Get default thankwords patterns",
            operationId = "getThankwords",
            path = "v1/data/thankwords",
            methods = HttpMethod.GET,
            tags = {"Data"},
            responses = {@OpenApiResponse(status = "200", content = @OpenApiContent(from = ThankwordsContainer.class))})
    public void getThankwords(Context ctx) {
        ctx.header("Cache-Control", "public, max-age=3600");
        ctx.json(thankwordsContainer);
    }

    @OpenApi(
            summary = "Get available languages",
            operationId = "getLanguages",
            path = "v1/data/languages",
            methods = HttpMethod.GET,
            tags = {"Data"},
            responses = {@OpenApiResponse(status = "200", content = @OpenApiContent(from = LanguageInfo[].class))})
    public void getLanguages(Context ctx) {
        ctx.header("Cache-Control", "public, max-age=86400");
        ctx.json(localization.languages());
    }

    @OpenApi(
            summary = "Get links",
            operationId = "getLinks",
            path = "v1/data/links",
            methods = HttpMethod.GET,
            tags = {"Data"},
            responses = {@OpenApiResponse(status = "200", content = @OpenApiContent(from = Links.class))})
    public void getLinks(Context ctx) {
        ctx.header("Cache-Control", "public, max-age=3600");
        ctx.json(configuration.links());
    }

    @OpenApi(
            summary = "Get token features",
            operationId = "getTokenFeatures",
            path = "v1/data/token_features",
            methods = HttpMethod.GET,
            tags = {"Data"},
            responses = {@OpenApiResponse(status = "200", content = @OpenApiContent(from = SKUFeatures.class))})
    public void getTokenFeatures(Context ctx) {
        ctx.header("Cache-Control", "public, max-age=3600");
        ctx.json(configuration.skus().features());
    }

    public void getAvailableSKUs(Context ctx) {
        ctx.header("Cache-Control", "public, max-age=3600");
        ctx.json(skus);
    }

    @OpenApi(
            summary = "Get asset by path",
            operationId = "getAsset",
            path = "v1/data/assets/{path}",
            methods = HttpMethod.GET,
            tags = {"Data"},
            pathParams = {@OpenApiParam(name = "path", description = "The path to the asset", required = true)},
            responses = {
                @OpenApiResponse(status = "200", content = @OpenApiContent(from = String.class)),
                @OpenApiResponse(status = "200", content = @OpenApiContent(from = Map.class))
            })
    public void getAsset(Context ctx) {
        String path = ctx.pathParam("*path");
        String html = markdownService.getHtml(path);
        if (html != null) {
            ctx.header("Cache-Control", "public, max-age=3600");
            ctx.html(html);
            return;
        }

        Map<String, String> directoryHtml = markdownService.getDirectoryHtml(path);
        if (!directoryHtml.isEmpty()) {
            ctx.header("Cache-Control", "public, max-age=3600");
            ctx.json(directoryHtml);
            return;
        }

        throw new NotFoundResponse();
    }

    @Override
    public void buildRoutes() {
        path("data", () -> {
            get("thankwords", this::getThankwords, Role.ANYONE);
            get("languages", this::getLanguages, Role.ANYONE);
            get("links", this::getLinks, Role.ANYONE);
            get("token_features", this::getTokenFeatures, Role.ANYONE);
            get("skus", this::getAvailableSKUs, Role.ANYONE);
            get("assets/{*path}", this::getAsset, Role.ANYONE);
        });
    }
}
