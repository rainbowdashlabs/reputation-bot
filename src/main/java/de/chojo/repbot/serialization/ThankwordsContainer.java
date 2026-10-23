/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.serialization;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "CanBeFinal"})
public class ThankwordsContainer {
    private Map<String, List<String>> defaults = new HashMap<>();

    /**
     * Loads the default thank words shipped with the bot.
     *
     * @return the default thank words per language
     * @throws IOException if the resource can't be read
     */
    public static ThankwordsContainer load() throws IOException {
        try (var input = ThankwordsContainer.class.getClassLoader().getResourceAsStream("Thankswords.json")) {
            return new ObjectMapper()
                    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                    .readValue(input, ThankwordsContainer.class);
        }
    }

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
