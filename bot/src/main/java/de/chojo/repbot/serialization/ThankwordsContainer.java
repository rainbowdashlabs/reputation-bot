/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.serialization;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class ThankwordsContainer {
    private Map<String, List<String>> defaults = new HashMap<>();

    public List<String> get(String key) {
        for (var entry : defaults.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Set<String> getAvailableLanguages() {
        return Collections.unmodifiableSet(defaults.keySet());
    }
}
