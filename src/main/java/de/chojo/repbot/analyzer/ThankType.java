package de.chojo.repbot.analyzer;

public enum ThankType {
    FUZZY("thankType.fuzzy"),
    MENTION("thankType.mention"),
    ANSWER("thankType.answer"),
    DIRECT("thankType.direct"),
    REACTION("thankType.reaction"),
    EMBED("thankType.embed"),
    NO_TARGET,
    NO_MATCH;

    private final String localizedName;

    ThankType(String localizedName) {
        this.localizedName = localizedName;
    }
    ThankType() {
        localizedName = null;
    }

    public String localizedName() {
        return localizedName;
    }
}
