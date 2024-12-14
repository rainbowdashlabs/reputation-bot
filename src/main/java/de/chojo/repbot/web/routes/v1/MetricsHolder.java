/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1;

import de.chojo.repbot.dao.provider.Metrics;
import de.chojo.repbot.web.error.ApiException;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.routes.v1.metrics.MetricCache;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpCode;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Abstract class for handling metrics-related routes.
 */
public abstract class MetricsHolder implements RoutesBuilder {
    private static final Logger log = getLogger(MetricsHolder.class);
    private final MetricCache cache;
    private final Metrics metrics;

    /**
     * Constructs a new MetricsHolder with the specified cache and metrics.
     *
     * @param cache   the metric cache
     * @param metrics the metrics provider
     */
    public MetricsHolder(MetricCache cache, Metrics metrics) {
        this.cache = cache;
        this.metrics = metrics;
    }

    /**
     * Returns the metrics provider.
     *
     * @return the metrics provider
     */
    public Metrics metrics() {
        return metrics;
    }

    /**
     * Writes an image to the HTTP context.
     *
     * @param ctx the HTTP context
     * @param png the image data in PNG format
     */
    protected void writeImage(Context ctx, byte[] png) {
        ctx.header("Content-Disposition", "filename=\"stats.png\"");
        ctx.header("X-Content-Type-Options", "nosniff");
        ctx.contentType("image/png");

        ctx.result(png).status(HttpStatus.OK_200);
    }

    /**
     * Retrieves and validates the offset parameter from the context.
     *
     * @param context the HTTP context
     * @param max     the maximum allowed value for the offset
     * @return the validated offset value
     * @throws ApiException if the offset is not a valid number or is out of range
     */
    protected int offset(Context context, int max) {
        var param = context.pathParam("offset");
        try {
            var offset = Integer.parseInt(param);
            assertSize(offset, 0, max);
            return offset;
        } catch (NumberFormatException e) {
            throw new ApiException(HttpCode.BAD_REQUEST, "Offset is not a number, Got: " + param);
        }
    }

    /**
     * Retrieves and validates the count parameter from the context.
     *
     * @param context the HTTP context
     * @param max     the maximum allowed value for the count
     * @return the validated count value
     * @throws ApiException if the count is not a valid number or is out of range
     */
    protected int count(Context context, int max) {
        var param = context.pathParam("count");
        try {
            var offset = Integer.parseInt(param);
            assertSize(offset, 2, max);
            return offset;
        } catch (NumberFormatException e) {
            throw new ApiException(HttpCode.BAD_REQUEST, "Count is not a number, Got: " + param);
        }
    }

    /**
     * Asserts that a value is within the specified range.
     *
     * @param value the value to check
     * @param min   the minimum allowed value
     * @param max   the maximum allowed value
     * @throws ApiException if the value is out of range
     */
    private void assertSize(int value, int min, int max) {
        if (value < min) {
            throw new ApiException(HttpCode.BAD_REQUEST, String.format("Value %s is too small. Min: %s", value, min));
        }
        if (value > max) {
            throw new ApiException(HttpCode.BAD_REQUEST, String.format("Value %s is too large. Max: %s", value, max));
        }
    }

    /**
     * Wraps a handler with caching functionality.
     *
     * @param handler the handler to wrap
     * @return the wrapped handler
     */
    public Handler cache(Handler handler) {
        return cache.cache(handler);
    }
}
