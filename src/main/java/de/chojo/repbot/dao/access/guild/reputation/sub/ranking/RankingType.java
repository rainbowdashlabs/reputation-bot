package de.chojo.repbot.dao.access.guild.reputation.sub.ranking;

public enum RankingType {
    RECEIVED,
    GIVEN;

    private final String localeKey;

    RankingType() {
        localeKey = "ranking." + name().toLowerCase();
    }

    public String localeKey() {
        return localeKey;
    }
}
