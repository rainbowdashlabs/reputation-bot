/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub.ranking.user;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.dao.access.guild.reputation.sub.ranking.RankingScope;
import de.chojo.repbot.dao.access.guild.reputation.sub.ranking.RankingType;
import de.chojo.repbot.dao.access.guild.reputation.sub.ranking.UserRankings;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.dao.pagination.Ranking;
import de.chojo.repbot.dao.snapshots.RankingEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.time.LocalDate;
import java.util.List;

public abstract class UserRanking implements GuildHolder {
    private final UserRankings user;
    private final RankingType type;

    protected UserRanking(UserRankings user, RankingType type) {
        this.user = user;
        this.type = type;
    }

    @Override
    public Guild guild() {
        return user.guild();
    }

    @Override
    public long guildId() {
        return user.guildId();
    }

    public Ranking defaultRanking(int pageSize, Member member) {
        return byMode(user.ranking().reputation().repGuild().settings().general().reputationMode(), pageSize, member);
    }

    public Ranking byMode(ReputationMode mode, int pageSize, Member member) {
        return new Ranking(title(mode), Replacement.create("USER", member.getEffectiveName()), () -> pages(pageSize, member, mode), page -> getRankingPage(pageSize, page, member, mode));
    }

    private String title(ReputationMode mode) {
        return "$%s$ - $%s$".formatted(type.localeKey(), RankingScope.USER.localeKey(mode));
    }

    protected abstract List<RankingEntry> getRankingPage(int pageSize, int page, Member member, ReputationMode mode);

    protected abstract int pages(int pageSize, Member member, ReputationMode mode);

    public UserRankings ranking() {
        return user;
    }

    public LocalDate resetDate() {
        return user.ranking().reputation().repGuild().settings().general().resetDate();
    }
}
