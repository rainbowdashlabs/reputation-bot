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

/**
 * Container class for managing thank words.
 */
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class ThankwordsContainer {
    private Map<String, List<String>> defaults = new HashMap<>();

    /**
     * Creates a new container with the default thank words.
     */
    public ThankwordsContainer(){
    }
    /**
     * Retrieves the list of thank words for the given key.
     *
     * @param key the key to look up
     * @return the list of thank words, or null if the key is not found
     */
    public List<String> get(String key) {
        for (var entry : defaults.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Retrieves the set of available languages.
     *
     * @return an unmodifiable set of available languages
     */
    public Set<String> getAvailableLanguages() {
        return Collections.unmodifiableSet(defaults.keySet());
    }
}
