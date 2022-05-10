package de.chojo.repbot.dataconsistency;

import de.chojo.jdautil.localization.util.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

public class TestLocalization {
    private static final Language[] languages = {Language.ENGLISH, Language.GERMAN,
            Language.of("es_ES", "Español"), Language.of("fr_FR", "Français"),
            Language.of("ru_RU", "Russian")};
    private static final Pattern replacements = Pattern.compile("%[a-zA-Z0-9.]+?%");

    @Test
    public void checkKeys() {
        Map<Language, ResourceBundle> resourceBundles = new HashMap<>();
        for (var code : languages) {
            var locale = code.toLocale();
            var bundle = ResourceBundle.getBundle("locale", locale);
            resourceBundles.put(code, bundle);
        }

        System.out.printf("Loaded %s languages!%n", languages.length);

        Set<String> keySet = new HashSet<>();
        for (var resourceBundle : resourceBundles.values()) {
            keySet.addAll(resourceBundle.keySet());
        }

        Map<String, Set<String>> replacements = new HashMap<>();
        var english = resourceBundles.get(Language.ENGLISH);
        for (var key : english.keySet()) {
            replacements.put(key, getReplacements(english.getString(key)));
            Assertions.assertFalse(english.getString(key).isBlank(), "Blank string at " + key + "@" + Language.ENGLISH);
        }

        for (var resourceBundle : resourceBundles.values()) {
            for (var key : keySet) {
                var id = key + "@" + resourceBundle.getLocale();
                var locale = resourceBundle.getString(key);
                Assertions.assertFalse(locale.isBlank(), "Blank or unlocalized key at " + id);
                var localeReplacements = getReplacements(locale);
                var defReplacements = replacements.get(key);
                Assertions.assertTrue(localeReplacements.containsAll(defReplacements),
                        "Missing replacement key in " + id
                        + ". Expected \"" + String.join(", ", defReplacements) + "\". Actual \"" + String.join(", ", localeReplacements) + "\"");
            }
        }
    }

    private Set<String> getReplacements(String message) {
        Set<String> found = new HashSet<>();
        var matcher = replacements.matcher(message);
        while (matcher.find()) {
            found.add(matcher.group());
        }
        return found;
    }

}
