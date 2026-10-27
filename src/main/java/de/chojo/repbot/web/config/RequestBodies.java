/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.config;

import io.javalin.http.Context;
import org.jetbrains.annotations.Nullable;

/**
 * Reads request bodies of routes that accept {@code null}. Every other route rejects a {@code null} body through the
 * {@link NullRejectingJsonMapper}.
 */
public final class RequestBodies {
    private RequestBodies() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Read the body of the request, treating an empty body and a JSON {@code null} as {@code null}.
     *
     * @param ctx  context of the request
     * @param type type of the body
     * @param <T>  type of the body
     * @return the body, or {@code null}
     */
    @Nullable
    public static <T> T nullable(Context ctx, Class<T> type) {
        if (isNull(ctx.body())) return null;
        return ctx.bodyAsClass(type);
    }

    /**
     * Check whether a JSON body carries no value.
     *
     * @param json the raw body
     * @return true if the body is empty or a JSON {@code null}
     */
    public static boolean isNull(String json) {
        return json.isBlank() || "null".equals(json.strip());
    }
}
