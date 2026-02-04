/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots;

import de.chojo.jdautil.util.MentionUtil;
import de.chojo.sadu.mapper.wrapper.Row;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.Optional;

public record RankingEntry(long rank, long userId, long value) {

    public static RankingEntry buildReceivedRanking(Row rs) throws SQLException {
        return new RankingEntry(rs.getLong("rank"),
                rs.getLong("user_id"),
                rs.getLong("reputation")
        );
    }

    public static RankingEntry buildGivenRanking(Row rs) throws SQLException {
        return new RankingEntry(
                rs.getLong("rank_donated"),
                rs.getLong("user_id"),
                rs.getLong("donated")
        );
    }

    public String fancyString(int maxRank) {
        var length = String.valueOf(maxRank).length();
        var rank = StringUtils.rightPad(String.valueOf(this.rank), length);
        return "`" + rank + "` **|** " + MentionUtil.user(userId) + " ➜ " + value;
    }

    public String simpleString() {
        return "%s ➜ %d".formatted(MentionUtil.user(userId), value);
    }

    public Optional<Member> resolveMember(Guild guild) {
        try {
            return Optional.ofNullable(guild.retrieveMemberById(userId()).complete());
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
