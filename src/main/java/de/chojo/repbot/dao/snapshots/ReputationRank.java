/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots;

import de.chojo.repbot.dao.access.guild.settings.sub.Ranks;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

/**
 * Represents a reputation rank.
 * <p>
 * A rank is {@link Comparable} and will be sorted from the highest reputation to lowest.
 */
public class ReputationRank implements GuildHolder, Comparable<ReputationRank> {
    private final long roleId;
    private final long reputation;
    private final Ranks ranks;
    private Role role;

    /**
     * Constructs a ReputationRank object with the specified ranks, role ID, and reputation.
     *
     * @param ranks the ranks object
     * @param roleId the role ID
     * @param reputation the reputation value
     */
    public ReputationRank(Ranks ranks, long roleId, long reputation) {
        this.ranks = ranks;
        this.roleId = roleId;
        this.reputation = reputation;
    }

    /**
     * Builds a ReputationRank object from the specified ranks and database row.
     *
     * @param ranks the ranks object
     * @param rs the database row
     * @return the constructed ReputationRank object
     * @throws SQLException if a database access error occurs
     */
    public static ReputationRank build(Ranks ranks, Row rs) throws SQLException {
        return new ReputationRank(ranks,
                rs.getLong("role_id"),
                rs.getLong("reputation")
        );
    }

    /**
     * Retrieves the role associated with this reputation rank for the specified guild.
     *
     * @param guild the guild
     * @return an optional containing the role if found, otherwise an empty optional
     */
    public Optional<Role> getRole(Guild guild) {
        if (role == null) {
            role = guild.getRoleById(roleId);
        }
        return Optional.ofNullable(role);
    }

    /**
     * Returns the role ID associated with this reputation rank.
     *
     * @return the role ID
     */
    public long roleId() {
        return roleId;
    }

    /**
     * Returns the reputation value associated with this reputation rank.
     *
     * @return the reputation value
     */
    public long reputation() {
        return reputation;
    }

    /**
     * Retrieves the role associated with this reputation rank.
     *
     * @return an optional containing the role if found, otherwise an empty optional
     */
    public Optional<Role> role() {
        return getRole(ranks.guild());
    }

    /**
     * Removes the reputation role from the database.
     *
     * @return true if the role was successfully removed, false otherwise
     */
    public boolean remove() {
        var success = query("DELETE FROM guild_ranks WHERE guild_id = ? AND role_id = ?;")
                .single(call().bind(guildId()).bind(roleId))
                .update()
                .changed();
        ranks.refresh();
        return success;
    }

    /**
     * Returns the guild associated with this reputation rank.
     *
     * @return the guild
     */
    @Override
    public Guild guild() {
        return ranks.guild();
    }

    /**
     * Returns the guild ID associated with this reputation rank.
     *
     * @return the guild ID
     */
    @Override
    public long guildId() {
        return ranks.guildId();
    }

    /**
     * Compares this reputation rank with another based on reputation.
     *
     * @param o the other reputation rank
     * @return a negative integer, zero, or a positive integer as this reputation rank is less than, equal to, or greater than the specified reputation rank
     */
    @Override
    public int compareTo(@NotNull ReputationRank o) {
        return Long.compare(o.reputation, reputation);
    }

    /**
     * Returns a string representation of the reputation rank.
     *
     * @return a string representation of the reputation rank
     */
    @Override
    public String toString() {
        return "ReputationRank{" +
               "roleId=" + roleId +
               ", reputation=" + reputation +
               '}';
    }
}
