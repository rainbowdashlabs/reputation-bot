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
import net.dv8tion.jda.api.entities.Guild;

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
    public Guild guild() {
        return rankings.guild();
    }

    @Override
    public long guildId() {
        return rankings.guildId();
    }

    private String title(ReputationMode mode) {
        return "$%s$ - $%s$".formatted(type.localeKey(), RankingScope.GUILD.localeKey(mode));
    }

    public Ranking defaultRanking(int pageSize) {
        return byMode(rankings.reputation().repGuild().settings().general().reputationMode(), pageSize);
    }

    public Ranking byMode(ReputationMode mode, int pageSize) {
        return new Ranking(title(mode),
                create("GUILD", guild().getName()),
                () -> pages(pageSize, mode.guildRanking()),
                page -> getRankingPage(pageSize, page, mode.guildRanking()));
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

    protected abstract List<RankingEntry> getRankingPage(int pageSize, int page, String table);

    protected abstract int pages(int pageSize, String table);
}
