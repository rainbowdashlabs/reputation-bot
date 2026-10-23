/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub.ranking;

import de.chojo.repbot.dao.access.guild.reputation.sub.Rankings;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.dao.pagination.Ranking;
import de.chojo.repbot.dao.snapshots.RankingEntry;

import java.time.Instant;
import java.util.List;

import static de.chojo.jdautil.localization.util.Replacement.create;

public abstract class GuildRanking implements GuildHolder {
    private final Rankings rankings;
    private final RankingType type;

    protected GuildRanking(Rankings rankings, RankingType type) {
        this.rankings = rankings;
        this.type = type;
    }

    @Override
    public GuildHolder guildHolder() {
        return rankings;
    }

    public Ranking defaultRanking(int pageSize) {
        return byMode(rankings.reputation().repGuild().settings().general().reputationMode(), pageSize);
    }

    public Ranking byMode(ReputationMode mode, int pageSize) {
        return new Ranking(
                this,
                title(mode),
                create("GUILD", guild().getName()),
                () -> pages(pageSize, mode),
                page -> getRankingPage(pageSize, page, mode));
    }

    /**
     * Get the ranking of the guild.
     *
     * @param pageSize the size of a page
     * @return a sorted list of reputation users
     */
    public Ranking total(int pageSize) {
        return byMode(ReputationMode.TOTAL, pageSize);
    }

    /**
     * The reset date that applies to a ranking in a reputation mode.
     *
     * @param mode the mode of the ranking
     * @return the reset date, or null if none applies
     */
    public Instant resetDate(ReputationMode mode) {
        return rankings.reputation().repGuild().settings().general().activeResetDate(mode);
    }

    protected abstract List<RankingEntry> getRankingPage(int pageSize, int page, ReputationMode mode);

    protected abstract int pages(int pageSize, ReputationMode mode);

    private String title(ReputationMode mode) {
        return "$%s$ - $%s$".formatted(type.localeKey(), RankingScope.GUILD.localeKey(mode));
    }
}
