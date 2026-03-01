/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

public record Result<T, V extends Enum<?>>(T result, V failureReason, boolean success) {

    public Result(T result, V failureReason) {
        this(result, failureReason, failureReason == null);
    }

    public static <T, V extends Enum<?>> Result<T, V> failure(V failureReason) {
        return new Result<>(null, failureReason);
    }

    public static <T, V extends Enum<?>> Result<T, V> success(T result) {
        return new Result<>(result, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !isSuccess();
    }
}
