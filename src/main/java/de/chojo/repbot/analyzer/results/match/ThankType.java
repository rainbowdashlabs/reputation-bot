package de.chojo.repbot.analyzer.results.match;

public enum ThankType {
    FUZZY("thankType.fuzzy"),
    MENTION("thankType.mention"),
    ANSWER("thankType.answer"),
    DIRECT("thankType.direct"),
    REACTION("thankType.reaction"),
    EMBED("thankType.embed");

    private final String localeKey;

    ThankType(String localeKey) {
        this.localeKey = localeKey;
    }

    public String localeKey() {
        return localeKey;
    }
}
