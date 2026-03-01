/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class LogNotify {
    /**
     * Will be send to error-log channel.
     */
    public static final Marker NOTIFY_ADMIN = createMarker("NOTIFY_ADMIN");
    /**
     * Will be sent to status-log.
     */
    public static final Marker STATUS = createMarker("STATUS");
    /**
     * Currently unused.
     */
    public static final Marker DISCORD = createMarker("DISCORD");

    private LogNotify() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    private static Marker createMarker(@NotNull String name, @NotNull Marker... children) {
        var marker = MarkerFactory.getMarker(name);
        for (var child : children) {
            marker.add(child);
        }
        return marker;
    }
}
