/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.metrics;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.repbot.web.routes.RoutesBuilder;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.util.RateLimiter;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.javalin.apibuilder.ApiBuilder.after;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Handles caching of metric responses to improve performance and reduce load.
 */
public class MetricCache implements RoutesBuilder {
    private static final Logger log = getLogger(MetricCache.class);
    private final RateLimiter rateLimiter;
    private final Cache<CacheKey, ResponseCache> cache = CacheBuilder.newBuilder()
                                                                     .expireAfterAccess(30, TimeUnit.MINUTES)
                                                                     .maximumSize(100)
                                                                     .build();

    /**
     * Constructs a MetricCache with a rate limiter.
     */
    public MetricCache() {
        rateLimiter = new RateLimiter(TimeUnit.MINUTES);
    }

    /**
     * Returns a handler that caches the response of the given handler.
     *
     * @param supplier the handler whose response should be cached
     * @return a handler that caches the response
     */
    public Handler cache(Handler supplier) {
        return ctx -> {
            var cacheKey = new CacheKey(ctx);
            var cacheValue = cache.getIfPresent(cacheKey);
            if (cacheValue != null) {
                rateLimiter.incrementCounter(ctx, 60);
                log.trace("Cache hit on {}.", ctx.path());
                cacheValue.apply(ctx);
            } else {
                log.trace("No cache value for {}.", ctx.path());
                rateLimiter.incrementCounter(ctx, 30);
                supplier.handle(ctx);
            }
        };
    }

    /**
     * Builds the routes for caching metric responses.
     */
    @Override
    public void buildRoutes() {
        after(ctx -> {
            var cacheKey = new CacheKey(ctx);
            cache.get(cacheKey, () -> {
                log.trace("Cached result for {}.", ctx.path());
                return new ResponseCache(ctx);
            });
        });
    }

    /**
     * Represents a cached response.
     */
    private static class ResponseCache {
        String route;
        String accept;
        Map<String, String> header;
        String contentType;
        int status;
        byte[] body;

        /**
         * Constructs a ResponseCache from the given context.
         *
         * @param ctx the context
         */
        ResponseCache(Context ctx) {
            route = ctx.path();
            accept = ctx.header("Accept");
            header = ctx.res().getHeaderNames().stream().collect(Collectors.toMap(e -> e, ctx.res()::getHeader));
            status = ctx.statusCode();
            contentType = ctx.res().getContentType();
            try (var in = ctx.resultInputStream()) {
                body = in.readAllBytes();
                ctx.result(body);
            } catch (IOException e) {
                log.error("Could not cache result", e);
            }
        }

        /**
         * Applies the cached response to the given context.
         *
         * @param ctx the context
         */
        void apply(Context ctx) {
            ctx.status(status);
            for (var header : header.entrySet()) {
                if (ctx.res().containsHeader(header.getKey())) continue;
                ctx.header(header.getKey(), header.getValue());
            }
            ctx.contentType(contentType);
            ctx.result(body);
        }
    }

    /**
     * Represents a key for caching responses.
     */
    private static class CacheKey {
        String route;
        String accept;

        /**
         * Constructs a CacheKey from the given context.
         *
         * @param ctx the context
         */
        CacheKey(Context ctx) {
            route = ctx.path();
            accept = ctx.header("Accept");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey cacheKey)) return false;

            if (!route.equals(cacheKey.route)) return false;
            return Objects.equals(accept, cacheKey.accept);
        }

        @Override
        public int hashCode() {
            var result = route.hashCode();
            result = 31 * result + (accept != null ? accept.hashCode() : 0);
            return result;
        }
    }
}
