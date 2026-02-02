/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.config;

public final class Endpoint {
    public static String derivePathFromClass(Class<?> clazz){
        return clazz.getName().replace("de.chojo.repbot.web.routes.", "").replace(".", "/");
    }
}
