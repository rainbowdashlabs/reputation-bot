/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.config;

import de.chojo.repbot.web.error.ApiException;
import io.javalin.http.HttpStatus;
import io.javalin.json.JsonMapper;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.stream.Stream;

/**
 * Delegates to another {@link JsonMapper} and rejects request bodies that deserialize to {@code null}, such as a
 * literal {@code null}, with a bad request instead of failing inside the route.
 */
public class NullRejectingJsonMapper implements JsonMapper {
    private final JsonMapper delegate;

    public NullRejectingJsonMapper(JsonMapper delegate) {
        this.delegate = delegate;
    }

    @NotNull
    @Override
    public String toJsonString(@NotNull Object obj, @NotNull Type type) {
        return delegate.toJsonString(obj, type);
    }

    @NotNull
    @Override
    public InputStream toJsonStream(@NotNull Object obj, @NotNull Type type) {
        return delegate.toJsonStream(obj, type);
    }

    @Override
    public void writeToOutputStream(@NotNull Stream<?> stream, @NotNull OutputStream outputStream) {
        delegate.writeToOutputStream(stream, outputStream);
    }

    @NotNull
    @Override
    public <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) {
        if (RequestBodies.isNull(json)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        return delegate.fromJsonString(json, targetType);
    }

    @NotNull
    @Override
    public <T> T fromJsonStream(@NotNull InputStream json, @NotNull Type targetType) {
        return delegate.fromJsonStream(json, targetType);
    }
}
