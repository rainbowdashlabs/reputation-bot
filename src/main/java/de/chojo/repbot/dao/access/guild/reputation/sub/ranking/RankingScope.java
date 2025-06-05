package de.chojo.repbot.dao.access.guild.reputation.sub.ranking;

import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;

public enum RankingScope {
    GUILD, USER;

    public String localeKey(ReputationMode mode) {
        return "ranking.%s.%s".formatted(name().toLowerCase(), mode.name().toLowerCase().replace("_", ""));
    }
}
