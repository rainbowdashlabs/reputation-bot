package de.chojo.repbot.web.routes.v1.metrics;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.repbot.web.routes.RoutesBuilder;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.javalin.apibuilder.ApiBuilder.after;
import static org.slf4j.LoggerFactory.getLogger;

public class MetricCache implements RoutesBuilder {
    private static final Logger log = getLogger(MetricCache.class);
    private final Cache<CacheKey, ResponseCache> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .maximumSize(100)
            .build();

    public Handler cache(Handler supplier) {
        return ctx -> {
            var cacheKey = new CacheKey(ctx);
            var cacheValue = cache.getIfPresent(cacheKey);
            if (cacheValue != null) {
                log.trace("Cache hit on {}.", ctx.path());
                cacheValue.apply(ctx);
            } else {
                log.trace("No cache value for {}.", ctx.path());
                supplier.handle(ctx);
            }
        };
    }

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

    private static class ResponseCache {
        String route;
        String accept;
        Map<String, String> header;
        String contentType;
        int status;
        byte[] body;

        ResponseCache(Context ctx) {
            route = ctx.path();
            accept = ctx.header("Accept");
            header = ctx.res.getHeaderNames().stream().collect(Collectors.toMap(e -> e, ctx.res::getHeader));
            status = ctx.status();
            contentType = ctx.res.getContentType();
            try (var in = ctx.resultStream()) {
                body = in.readAllBytes();
                log.trace("wrote");
                ctx.result(body);
            } catch (IOException e) {
                log.error("Could not cache result", e);
            }
        }

        void apply(Context ctx) {
            ctx.status(status);
            for (var header : header.entrySet()) {
                if (ctx.res.containsHeader(header.getKey())) continue;
                ctx.header(header.getKey(), header.getValue());
            }
            ctx.contentType(contentType);
            ctx.result(body);
        }
    }

    private static class CacheKey {
        String route;
        String accept;

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
            int result = route.hashCode();
            result = 31 * result + (accept != null ? accept.hashCode() : 0);
            return result;
        }
    }
}
