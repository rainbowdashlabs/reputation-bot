package de.chojo.repbot.dataconsistency;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

public class TestLocalization {
    private static final Pattern LOCALIZATION_CODE = Pattern.compile("\\$([a-zA-Z.]+?)\\$");
    private static final Pattern SIMPLE_LOCALIZATION_CODE = Pattern.compile("\"([a-zA-Z]+?\\.[a-zA-Z.]+)\"");
    private static final Set<String> WHITELIST = Set.of("bot.config", "bot.testmode");
    private static final Set<String> WHITELIST_ENDS = Set.of(".gg", ".com", "bot.config", ".png", ".json");

    private static final DiscordLocale[] languages = {
            DiscordLocale.ENGLISH_US,
            DiscordLocale.GERMAN,
            DiscordLocale.SPANISH,
            DiscordLocale.FRENCH,
            DiscordLocale.PORTUGUESE_BRAZILIAN,
            DiscordLocale.RUSSIAN
    };

    private static final Pattern replacements = Pattern.compile("%[a-zA-Z0-9.]+?%");

    @Test
    public void checkKeys() {
        Map<DiscordLocale, ResourceBundle> resourceBundles = new HashMap<>();
        for (var code : languages) {
            var locale = Locale.forLanguageTag(code.getLocale());
            var bundle = ResourceBundle.getBundle("locale", locale);
            resourceBundles.put(code, bundle);
        }

        System.out.printf("Loaded %s languages!%n", languages.length);

        Set<String> keySet = new HashSet<>();
        for (var resourceBundle : resourceBundles.values()) {
            keySet.addAll(resourceBundle.keySet());
        }

        Map<String, Set<String>> replacements = new HashMap<>();
        var english = resourceBundles.get(DiscordLocale.ENGLISH_US);
        for (var key : english.keySet()) {
            replacements.put(key, getReplacements(english.getString(key)));
            Assertions.assertFalse(english.getString(key)
                                          .isBlank(), "Blank string at " + key + "@" + DiscordLocale.ENGLISH_US);
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

    @Test
    public void detectMissingKeys() {
        var keys = ResourceBundle.getBundle("locale").keySet();
        List<Path> files;
        try (var stream = Files.walk(Path.of("src", "main", "java"))) {
            files = stream
                    .filter(p -> p.toFile().isFile())
                    .toList();

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        var count = 0;

        Set<String> foundKeys = new HashSet<>();

        for (var file : files) {
            int localCount = 0;
            List<String> content;
            try {
                content = Files.readAllLines(file);
            } catch (IOException e) {
                System.out.println("Could not read file");
                e.printStackTrace();
                continue;
            }

            for (var line : content) {
                var matcher = SIMPLE_LOCALIZATION_CODE.matcher(line);
                while (matcher.find()) {
                    count++;
                    localCount++;
                    var key = matcher.group(1);
                    foundKeys.add(key);
                    Assertions.assertTrue(keys.contains(key) || whitelisted(key), "Found unkown key \"" + key + "\" in " + file);
                }

                matcher = LOCALIZATION_CODE.matcher(line);
                while (matcher.find()) {
                    count++;
                    localCount++;
                    var key = matcher.group(1);
                    foundKeys.add(key);
                    Assertions.assertTrue(keys.contains(key) || whitelisted(key), "Found unkown key \"" + key + "\" in " + file);
                }
            }
            System.out.println("Found " + localCount + " key in " + file);
        }
        System.out.println("Found a total of " + count + " keys in " + files.size() + " files.");

        keys.removeAll(foundKeys);
        System.out.println("Found " + keys.size() + " without any direct usage in the code.");
        for (String key : keys) {
            System.out.println(key);
        }
    }

    private boolean whitelisted(String key) {
        if (WHITELIST.contains(key)) return true;
        for (String end : WHITELIST_ENDS) {
            if (key.endsWith(end)) return true;
        }
        return false;
    }
}
