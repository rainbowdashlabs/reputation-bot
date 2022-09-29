package de.chojo.repbot.analyzer.results.match;

public enum ThankType {
    FUZZY("thankType.fuzzy.name"),
    MENTION("thankType.mention.name"),
    ANSWER("thankType.answer.name"),
    DIRECT("thankType.direct.name"),
    REACTION("thankType.reaction.name"),
    EMBED("thankType.embed.name");

    private final String nameLocaleKey;

    ThankType(String nameLocaleKey) {
        this.nameLocaleKey = nameLocaleKey;
    }

    public String nameLocaleKey() {
        return nameLocaleKey;
    }
}
