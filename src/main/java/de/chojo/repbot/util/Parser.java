/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class Parser {
    private Parser() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    @Nullable
    public static <T extends Enum<T>> T parseEnum(String value, Class<T> enumClass) {
        for (T constant : enumClass.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(value)) {
                return constant;
            }
        }
        return null;
    }

    public static <T extends Enum<T>> Enum<T> parseEnum(String value, Class<T> enumClass, Enum<T> defaultValue) {
        return Objects.requireNonNullElse(parseEnum(value, enumClass), defaultValue);
    }
}
