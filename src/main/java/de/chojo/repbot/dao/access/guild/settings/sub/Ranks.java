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

/**
 * Manages the reputation ranks for a guild.
 */
public class Ranks implements GuildHolder {
    private final LinkedHashSet<ReputationRank> ranks = new LinkedHashSet<>();
    private final Settings settings;
    private final AtomicBoolean stackRoles;

    /**
     * Constructs a Ranks instance.
     *
     * @param settings   the settings for the guild
     * @param stackRoles whether to stack roles
     */
    public Ranks(Settings settings, AtomicBoolean stackRoles) {
        this.settings = settings;
        this.stackRoles = stackRoles;
    }

    /**
     * Adds a reputation rank.
     * <p>
     * If the role or the reputation amount is already in use, it will be removed first.
     *
     * @param role       the role
     * @param reputation the required reputation of the role
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

    /**
     * Retrieves the list of reputation ranks.
     *
     * @return the list of reputation ranks
     */
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
     * @param user the user to check
     * @return the list of ranks
     */
    public List<ReputationRank> currentRanks(RepUser user) {
        var profile = user.profile();
        return ranks().stream()
                      .filter(rank -> rank.reputation() <= profile.reputation())
                      .sorted()
                      .limit(stackRoles.get() ? Integer.MAX_VALUE : 1)
                      .toList();
    }

    /**
     * Gets the current rank of the user.
     *
     * @param user the user to check
     * @return an optional containing the current rank if found, otherwise empty
     */
    public Optional<ReputationRank> currentRank(RepUser user) {
        var profile = user.profile();
        return ranks().stream()
                      .filter(rank -> rank.reputation() <= profile.reputation())
                      .sorted()
                      .limit(1)
                      .findFirst();
    }

    /**
     * Gets the next rank of the user.
     *
     * @param user the user to check
     * @return an optional containing the next rank if found, otherwise empty
     */
    public Optional<ReputationRank> nextRank(RepUser user) {
        var profile = user.profile();
        return ranks().stream().filter(rank -> rank.reputation() > profile.reputation())
                      .sorted(Comparator.reverseOrder()).limit(1).findFirst();
    }

    /**
     * Retrieves the guild associated with this instance.
     *
     * @return the guild
     */
    @Override
    public Guild guild() {
        return settings.guild();
    }

    /**
     * Retrieves the guild ID associated with this instance.
     *
     * @return the guild ID
     */
    @Override
    public long guildId() {
        return settings.guildId();
    }

    /**
     * Gets the rank associated with the specified role.
     *
     * @param role the role
     * @return an optional containing the rank if found, otherwise empty
     */
    public Optional<ReputationRank> rank(Role role) {
        return query("SELECT reputation FROM guild_ranks WHERE guild_id = ? AND role_id = ?")
                .single(call().bind(guildId()).bind(role.getIdLong()))
                .map(row -> new ReputationRank(this, role.getIdLong(), row.getInt("reputation")))
                .first();
    }

    /**
     * Refreshes the ranks by clearing the current list.
     */
    public void refresh() {
        ranks.clear();
    }

    /**
     * Generates a pretty string representation of the ranks.
     *
     * @return a pretty string representation of the ranks
     */
    public String prettyString() {
        return ranks().stream().filter(r -> r.role().isPresent())
                      .map(rank -> "%s(%d) %d".formatted(rank.role().get().getName(), rank.role().get().getPosition(), rank.reputation()))
                      .collect(Collectors.joining("\n"));
    }
}
