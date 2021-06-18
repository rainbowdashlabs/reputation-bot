package de.chojo.repbot.serialization;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ThankwordsContainer {
    private final Map<String, List<String>> defaults = new HashMap<>();

    public List<String> get(String key) {
        return defaults.get(key);
    }

    public Set<String> getAvailableLanguages() {
        return Collections.unmodifiableSet(defaults.keySet());
    }
}
