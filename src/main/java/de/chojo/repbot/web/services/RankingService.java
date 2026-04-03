/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.services;

import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.pagination.Ranking;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.web.cache.MemberCache;
import de.chojo.repbot.web.pojo.ranking.RankingEntryPOJO;
import de.chojo.repbot.web.pojo.ranking.RankingPagePOJO;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public class RankingService {

    private final GuildRepository guildRepository;
    private final MemberCache memberCache;

    public RankingService(GuildRepository guildRepository, MemberCache memberCache) {
        this.guildRepository = guildRepository;
        this.memberCache = memberCache;
    }

    public RankingPagePOJO getGuildGiven(Guild guild, ReputationMode mode, int pageSize, int page) {
        var repGuild = guildRepository.guild(guild);
        var ranking = repGuild.reputation().ranking().given().byMode(mode, pageSize);
        return toPage(guild, ranking, page);
    }

    public RankingPagePOJO getGuildReceived(Guild guild, ReputationMode mode, int pageSize, int page) {
        var repGuild = guildRepository.guild(guild);
        var ranking = repGuild.reputation().ranking().received().byMode(mode, pageSize);
        return toPage(guild, ranking, page);
    }

    public RankingPagePOJO getUserGiven(Guild guild, Member member, ReputationMode mode, int pageSize, int page) {
        var repGuild = guildRepository.guild(guild);
        var ranking = repGuild.reputation().ranking().user().given().byMode(mode, pageSize, member);
        return toPage(guild, ranking, page);
    }

    public RankingPagePOJO getUserReceived(Guild guild, Member member, ReputationMode mode, int pageSize, int page) {
        var repGuild = guildRepository.guild(guild);
        var ranking = repGuild.reputation().ranking().user().received().byMode(mode, pageSize, member);
        return toPage(guild, ranking, page);
    }

    private RankingPagePOJO toPage(Guild guild, Ranking ranking, int page) {
        int pages = ranking.pages();
        List<RankingEntryPOJO> entries = ranking.page(page).stream()
                .map(entry -> new RankingEntryPOJO(entry.rank(), entry.value(), memberCache.get(guild, entry.userId())))
                .toList();
        return new RankingPagePOJO(pages, page, entries);
    }
}
