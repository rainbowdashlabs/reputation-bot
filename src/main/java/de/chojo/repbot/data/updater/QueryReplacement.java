package de.chojo.repbot.data.updater;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class QueryReplacement {
    String target;
    String replacement;

    public String apply(String source) {
        return source.replaceAll(target, replacement);
    }
}
