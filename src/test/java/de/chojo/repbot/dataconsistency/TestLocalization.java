/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dataconsistency;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestLocalization {
    private static final Pattern LOCALIZATION_CODE = Pattern.compile("\\$([a-zA-Z.]+?)\\$");
    private static final Pattern SIMPLE_LOCALIZATION_CODE = Pattern.compile("\"([a-zA-Z]+?\\.[a-zA-Z.]+)\"");
    private static final Pattern REPLACEMENTS = Pattern.compile("%[a-zA-Z\\d.]+?%");
    private static final Set<String> WHITELIST = Set.of("bot.config", "bot.testmode", "bot.cleancommands", "bot.gdpr.enable", "yyyy.MM.dd");
    private static final Set<String> WHITELIST_ENDS = Set.of(".gg", ".com", "bot.config", ".png", ".json");

    private static final DiscordLocale[] LOCALES = {
            DiscordLocale.ENGLISH_US,
            DiscordLocale.GERMAN,
            DiscordLocale.SPANISH,
            DiscordLocale.FRENCH,
            DiscordLocale.PORTUGUESE_BRAZILIAN,
            DiscordLocale.RUSSIAN
    };


    @Test
    public void checkKeys() {
        Map<DiscordLocale, ResourceBundle> resourceBundles = new EnumMap<>(DiscordLocale.class);
        for (var code : LOCALES) {
            var bundle = ResourceBundle.getBundle("locale", Locale.forLanguageTag(code.getLocale()));
            resourceBundles.put(code, bundle);
        }

        System.out.printf("Loaded %s languages!%n", LOCALES.length);

        Set<String> keySet = new HashSet<>();
        for (var resourceBundle : resourceBundles.values()) {
            keySet.addAll(resourceBundle.keySet());
        }

        Map<String, Set<String>> replacements = new HashMap<>();
        var english = resourceBundles.get(DiscordLocale.ENGLISH_US);
        for (var key : english.keySet()) {
            replacements.put(key, getReplacements(english.getString(key)));
            Assertions.assertFalse(english.getString(key).isBlank(),
                    "Blank string at " + key + "@" + DiscordLocale.ENGLISH_US);
        }

        for (var resourceBundle : resourceBundles.values()) {
            for (var key : keySet) {
                var keyLoc = key + "@" + resourceBundle.getLocale();
                var locale = resourceBundle.getString(key);
                Assertions.assertFalse(locale.isBlank(), "Blank or unlocalized key at " + keyLoc);
                var localeReplacements = getReplacements(locale);
                var defReplacements = replacements.get(key);
                Assertions.assertTrue(localeReplacements.containsAll(defReplacements),
                        "Missing replacement key in " + keyLoc
                        + ". Expected \"" + String.join(", ", defReplacements) + "\". Actual \"" + String.join(", ", localeReplacements) + "\"");
            }
        }
    }

    private Set<String> getReplacements(String message) {
        Set<String> found = new HashSet<>();
        var matcher = REPLACEMENTS.matcher(message);
        while (matcher.find()) {
            found.add(matcher.group());
        }
        return found;
    }

    @Test
    public void detectMissingKeys() throws IOException {
        var keys = ResourceBundle.getBundle("locale").keySet();
        List<Path> files;
        try (var stream = Files.walk(Path.of("src", "main", "java"))) {
            files = stream
                    .filter(p -> p.toFile().isFile())
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        try (var stream = Files.walk(Path.of("src", "main", "resources"))) {
            files.addAll(stream
                    .filter(p -> p.toFile().isFile())
                    .filter(p -> p.getFileName().startsWith("locale"))
                    .toList());
        }

        var count = 0;

        Set<String> foundKeys = new HashSet<>();

        for (var file : files) {
            var localCount = 0;
            List<String> content;
            content = Files.readAllLines(file);

            var currentLine = 0;

            for (var line : content) {
                currentLine++;
                var matcher = SIMPLE_LOCALIZATION_CODE.matcher(line);
                while (matcher.find()) {
                    count++;
                    localCount++;
                    var key = matcher.group(1);
                    foundKeys.add(key);
                    Assertions.assertTrue(keys.contains(key) || whitelisted(key), "Found unknown key \"" + key + "\" in " + file + " at line " + currentLine);
                }

                matcher = LOCALIZATION_CODE.matcher(line);
                while (matcher.find()) {
                    count++;
                    localCount++;
                    var key = matcher.group(1);
                    foundKeys.add(key);
                    Assertions.assertTrue(keys.contains(key) || whitelisted(key), "Found unknown key \"" + key + "\" in " + file + " at line " + currentLine);
                }
            }
            System.out.println("Found " + localCount + " key in " + file);
        }
        System.out.println("Found a total of " + count + " keys in " + files.size() + " files.");

        keys.removeAll(foundKeys);
        System.out.println("Found " + keys.size() + " without any direct usage in the code.");
        for (var key : keys) {
            System.out.println(key);
        }
    }

    private boolean whitelisted(String key) {
        if (WHITELIST.contains(key)) return true;
        for (var end : WHITELIST_ENDS) {
            if (key.endsWith(end)) return true;
        }
        return false;
    }
}
