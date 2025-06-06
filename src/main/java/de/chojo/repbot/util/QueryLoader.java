/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import java.io.IOException;

public class QueryLoader {
    public static String loadQuery(String... names) {
        String path = "queries/" + String.join("/", names) + ".sql";
        try {
            return new String(QueryLoader.class.getClassLoader().getResourceAsStream(path).readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Could not load query: " + path, e);
        }
    }
}
