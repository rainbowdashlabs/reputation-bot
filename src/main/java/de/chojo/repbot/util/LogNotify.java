/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Utility class for creating and managing log markers.
 */
public final class LogNotify {
    /**
     * Marker for notifications to be sent to the error-log channel.
     */
    public static final Marker NOTIFY_ADMIN = createMarker("NOTIFY_ADMIN");
    /**
     * Marker for notifications to be sent to the status-log.
     */
    public static final Marker STATUS = createMarker("STATUS");
    /**
     * Marker currently unused.
     */
    public static final Marker DISCORD = createMarker("DISCORD");

    /**
     * Private constructor to prevent instantiation.
     * Throws an UnsupportedOperationException if called.
     */
    private LogNotify() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    /**
     * Creates a new marker with the specified name and optional child markers.
     *
     * @param name the name of the marker
     * @param children optional child markers to add to the created marker
     * @return the created marker
     */
    private static Marker createMarker(@NotNull String name, @NotNull Marker... children) {
        var marker = MarkerFactory.getMarker(name);
        for (var child : children) {
            marker.add(child);
        }
        return marker;
    }
}
