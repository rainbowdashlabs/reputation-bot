/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

import de.chojo.repbot.dao.access.guild.reputation.sub.RepUser;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.dao.snapshots.ReputationRank;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class Ranks implements GuildHolder {
    private final LinkedHashSet<ReputationRank> ranks = new LinkedHashSet<>();
    private final Settings settings;
    private final AtomicBoolean stackRoles;

    public Ranks(Settings settings, AtomicBoolean stackRoles) {
        this.settings = settings;
        this.stackRoles = stackRoles;
    }

    /**
     * Add a reputation rank.
     * <p>
     * If the role or the reputation amount is already in use it will be removed first.
     *
     * @param role       role
     * @param reputation required reputation of role
     * @return true if the role was added or updated
     */
    public boolean add(Role role, long reputation) {
        var deleteRank = query("""
                DELETE FROM
                    guild_ranks
                WHERE
                    guild_id = ?
                        AND (role_id = ?
                            OR reputation = ?);
                """)
                .single(call().bind(guildId()).bind(role.getIdLong()).bind(reputation))
                .delete()
                .changed();

        var insertRank = query("""
                INSERT INTO guild_ranks(guild_id, role_id, reputation) VALUES(?,?,?)
                    ON CONFLICT(guild_id, role_id)
                        DO UPDATE
                            SET reputation = excluded.reputation,
                                role_id = excluded.role_id;
                """)
                .single(call().bind(guildId()).bind(role.getIdLong()).bind(reputation))
                .update()
                .changed();
        if (deleteRank && insertRank) {
            ranks.removeIf(r -> r.roleId() == role.getIdLong() || reputation == r.reputation());
            ranks.add(new ReputationRank(this, role.getIdLong(), reputation));
        }
        return deleteRank;
    }

    public List<ReputationRank> ranks() {
        if (!ranks.isEmpty()) {
            return ranks.stream().sorted().toList();
        }
        var ranks = query("""
                SELECT
                    role_id,
                    reputation
                FROM
                    guild_ranks
                WHERE guild_id = ?
                ORDER BY reputation;
                """)
                .single(call().bind(guildId()))
                .map(r -> ReputationRank.build(this, r))
                .all();
        this.ranks.addAll(ranks);
        return this.ranks.stream().sorted().toList();
    }

    /**
     * Gets all reputation ranks which should be assigned to the user.
     * <p>
     * This will always contain zero or one role when {@link General#stackRoles()} is true.
     * <p>
     * This will contain up to {@link #ranks()}.size() when {@link General#stackRoles()} is false.
     *
     * @param user user to check
     * @return list of ranks
     */
    public List<ReputationRank> currentRanks(RepUser user) {
        var profile = user.profile();
        return ranks().stream()
                      .filter(rank -> rank.reputation() <= profile.reputation())
                      .sorted()
                      .limit(stackRoles.get() ? Integer.MAX_VALUE : 1)
                      .toList();
    }

    public Optional<ReputationRank> currentRank(RepUser user) {
        var profile = user.profile();
        return ranks().stream()
                      .filter(rank -> rank.reputation() <= profile.reputation())
                      .sorted()
                      .limit(1)
                      .findFirst();
    }

    public Optional<ReputationRank> nextRank(RepUser user) {
        var profile = user.profile();
        return ranks().stream().filter(rank -> rank.reputation() > profile.reputation())
                      .sorted(Comparator.reverseOrder()).limit(1).findFirst();
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }

    @Override
    public long guildId() {
        return settings.guildId();
    }

    public Optional<ReputationRank> rank(Role role) {
        return query("SELECT reputation FROM guild_ranks WHERE guild_id = ? AND role_id = ?")
                .single(call().bind(guildId()).bind(role.getIdLong()))
                .map(row -> new ReputationRank(this, role.getIdLong(), row.getInt("reputation")))
                .first();
    }

    public void refresh() {
        ranks.clear();
    }

    public String prettyString() {
        return ranks().stream().filter(r -> r.role().isPresent())
                      .map(rank -> "%s(%d) %d".formatted(rank.role().get().getName(), rank.role().get().getPosition(), rank.reputation()))
                      .collect(Collectors.joining("\n"));
    }
}
