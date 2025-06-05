/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub.ranking.user;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.dao.pagination.Ranking;
import de.chojo.repbot.dao.snapshots.RankingEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public abstract class UserRanking implements GuildHolder {
    private final de.chojo.repbot.dao.access.guild.reputation.sub.ranking.UserRanking user;

    protected UserRanking(de.chojo.repbot.dao.access.guild.reputation.sub.ranking.UserRanking user) {
        this.user = user;
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
        return switch (mode) {
            case TOTAL -> total(pageSize, member);
            case ROLLING_WEEK -> days7(pageSize, member);
            case ROLLING_MONTH -> days30(pageSize, member);
            case WEEK -> week(pageSize, member);
            case MONTH -> month(pageSize, member);
            default -> throw new IllegalArgumentException("Unknown input " + mode);
        };
    }

    /**
     * Get the ranking of the guild.
     *
     * @param pageSize the size of a page
     * @param member
     * @return a sorted list of reputation users
     */
    public Ranking total(int pageSize, Member member) {
        return getGuildRanking("command.top.message.total", pageSize, member, ReputationMode.TOTAL);
    }

    /**
     * Get the 7 days ranking of the guild.
     *
     * @param pageSize the size of a page
     * @param member
     * @return a sorted list of reputation users
     */
    public Ranking days7(int pageSize, Member member) {
        return getGuildRanking("command.top.message.rollingweektitle", pageSize, member, ReputationMode.ROLLING_WEEK);
    }

    /**
     * Get the 30 days ranking of the guild.
     *
     * @param pageSize the size of a page
     * @param member
     * @return a sorted list of reputation users
     */
    public Ranking days30(int pageSize, Member member) {
        return getGuildRanking("command.top.message.rollingmonthtitle", pageSize, member, ReputationMode.ROLLING_MONTH);
    }

    /**
     * Get the weekly ranking of the guild.
     *
     * @param pageSize the size of a page
     * @param member
     * @return a sorted list of reputation users
     */
    public Ranking week(int pageSize, Member member) {
        return getGuildRanking("command.top.message.weekTitle", pageSize, member, ReputationMode.WEEK);
    }

    /**
     * Get the monthly ranking of the guild.
     *
     * @param pageSize the size of a page
     * @param member
     * @return a sorted list of reputation users
     */
    public Ranking month(int pageSize, Member member) {
        return getGuildRanking("command.top.message.monthTitle", pageSize, member, ReputationMode.MONTH);
    }

    private Ranking getGuildRanking(String title, int pageSize, Member member, ReputationMode mode){
        return new Ranking(title, Replacement.create("USER", member.getEffectiveName()), () -> pages(pageSize, member, mode), page -> getRankingPage(pageSize, page, member, mode));
    }

    protected abstract List<RankingEntry> getRankingPage(int pageSize, int page, Member member, ReputationMode mode);

    protected abstract int pages(int pageSize, Member member, ReputationMode mode);

}
