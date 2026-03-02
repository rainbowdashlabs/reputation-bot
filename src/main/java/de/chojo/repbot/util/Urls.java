package de.chojo.repbot.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Urls {

    public static Map<String, String> splitQuery(String url) {
        return Arrays.stream(url.split("&"))
                .map(Urls::splitQueryParameter)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map.Entry<String, String> splitQueryParameter(String it) {
        final int idx = it.indexOf("=");
        final String key = idx > 0 ? it.substring(0, idx) : it;
        final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
        return new AbstractMap.SimpleImmutableEntry<>(
                URLDecoder.decode(key, StandardCharsets.UTF_8),
                URLDecoder.decode(value, StandardCharsets.UTF_8)
        );
    }
}
