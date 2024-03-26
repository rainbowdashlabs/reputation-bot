/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.access.guild.reputation.sub.Analyzer;
import de.chojo.repbot.dao.access.guild.reputation.sub.Log;
import de.chojo.repbot.dao.access.guild.reputation.sub.Ranking;
import de.chojo.repbot.dao.access.guild.reputation.sub.RepUser;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.dao.snapshots.GuildReputationStats;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static org.slf4j.LoggerFactory.getLogger;

public class Reputation  implements GuildHolder {
    private static final Logger log = getLogger(Reputation.class);
    private final RepGuild repGuild;

    private final Ranking ranking;
    private final Cache<Long, RepUser> users = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
    private final Log logAccess;
    private final Analyzer analyzer;

    public Reputation(RepGuild repGuild) {
        this.repGuild = repGuild;
        ranking = new Ranking(this);
        logAccess = new Log(this);
        analyzer = new Analyzer(this);
    }

    public Log log() {
        return logAccess;
    }

    public Analyzer analyzer() {
        return analyzer;
    }

    @Override
    public Guild guild() {
        return repGuild.guild();
    }

    @Override
    public long guildId() {
        return repGuild().guildId();
    }

    public GuildReputationStats stats() {
        return query("SELECT total_reputation, week_reputation, today_reputation, top_channel FROM get_guild_stats(?)")
                .single(call().bind(guildId()))
                .map(rs -> new GuildReputationStats(
                        rs.getInt("total_reputation"),
                        rs.getInt("week_reputation"),
                        rs.getInt("today_reputation"),
                        rs.getLong("top_channel")
                )).first()
                .orElseGet(() -> new GuildReputationStats(0, 0, 0, 0));
    }

    public RepUser user(@NotNull Member member) {
        try {
            return users.get(member.getIdLong(), () -> new RepUser(this, member)).refresh(member);
        } catch (ExecutionException e) {
            log.error("Could not create reputation user", e);
            throw new RuntimeException(e);
        }
    }

    public RepUser user(User user) {
        try {
            return users.get(user.getIdLong(), () -> new RepUser(this, user));
        } catch (ExecutionException e) {
            log.error("Could not create reputation user", e);
            throw new RuntimeException(e);
        }
    }

    public Ranking ranking() {
        return ranking;
    }

    public RepGuild repGuild() {
        return repGuild;
    }

    public Configuration configuration() {
        return repGuild.configuration();
    }
}
