/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.metrics.util;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.web.error.ApiException;
import de.chojo.repbot.web.routes.RoutesBuilder;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class MetricsHolder implements RoutesBuilder {
    private static final Logger log = getLogger(MetricsHolder.class);
    private final MetricCache cache;
    private final Metrics metrics;

    public MetricsHolder(MetricCache cache, Metrics metrics) {
        this.cache = cache;
        this.metrics = metrics;
    }

    public Metrics metrics() {
        return metrics;
    }

    public Handler cache(Handler handler) {
        return cache.cache(handler);
    }

    protected void writeImage(Context ctx, byte[] png) {
        ctx.header("Content-Disposition", "filename=\"stats.png\"");
        ctx.header("X-Content-Type-Options", "nosniff");
        ctx.contentType("image/png");

        ctx.result(png).status(HttpStatus.OK);
    }

    protected int offset(Context context, int max) {
        var param = context.pathParam("offset");
        try {
            var offset = Integer.parseInt(param);
            assertSize(offset, 0, max);
            return offset;
        } catch (NumberFormatException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Offset is not a number, Got: " + param);
        }
    }

    protected int count(Context context, int max) {
        var param = context.pathParam("count");
        try {
            var offset = Integer.parseInt(param);
            assertSize(offset, 2, max);
            return offset;
        } catch (NumberFormatException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Count is not a number, Got: " + param);
        }
    }

    private void assertSize(int value, int min, int max) {
        if (value < min) {
            throw new ApiException(HttpStatus.BAD_REQUEST, String.format("Value %s is too small. Min: %s", value, min));
        }
        if (value > max) {
            throw new ApiException(HttpStatus.BAD_REQUEST, String.format("Value %s is too large. Max: %s", value, max));
        }
    }
}
