/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.components.GuildHolder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Abstract class representing a holder for roles in a guild.
 */
public abstract class RolesHolder implements GuildHolder {
    /**
     * Logger for logging events.
     */
    private static final Logger log = getLogger(RolesHolder.class);
    /**
     * Set of role IDs.
     */
    protected final Set<Long> roleIds;
    /**
     * Thanking settings.
     */
    protected final Thanking thanking;

    /**
     * Constructs a new RolesHolder.
     *
     * @param thanking the thanking settings
     * @param roleIds the set of role IDs
     */
    public RolesHolder(Thanking thanking, Set<Long> roleIds) {
        this.thanking = thanking;
        this.roleIds = roleIds;
    }

    /**
     * Checks if the member has any of the roles in the roleIds set.
     *
     * @param member the member to check
     * @return true if the member has any of the roles, false otherwise
     */
    public boolean hasRole(@Nullable Member member) {
        if (member == null) {
            log.trace("Member is null. Could not determine group.");
            return false;
        }
        if (roleIds.isEmpty()) return true;
        for (var role : member.getRoles()) {
            if (roleIds.contains(role.getIdLong())) return true;
        }
        return false;
    }

    /**
     * Retrieves the set of roles corresponding to the role IDs.
     *
     * @return the set of roles
     */
    public Set<Role> roles() {
        return roleIds.stream().map(guild()::getRoleById).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /**
     * Retrieves the guild associated with this RolesHolder.
     *
     * @return the guild
     */
    @Override
    public Guild guild() {
        return thanking.guild();
    }

    /**
     * Retrieves the target table name for database operations.
     *
     * @return the target table name
     */
    protected abstract String targetTable();

    /**
     * Adds a role to the roleIds set and the database.
     *
     * @param role the role to add
     * @return true if the role was added, false otherwise
     */
    public boolean add(Role role) {
        var result = query("INSERT INTO %s(guild_id, role_id) VALUES (?,?) ON CONFLICT(guild_id, role_id) DO NOTHING", targetTable())
                .single(call().bind(guildId()).bind(role.getIdLong()))
                .update()
                .changed();
        if (result) {
            roleIds.add(role.getIdLong());
        }
        return result;
    }

    /**
     * Removes a role from the roleIds set and the database.
     *
     * @param role the role to remove
     * @return true if the role was removed, false otherwise
     */
    public boolean remove(Role role) {
        var result = query("DELETE FROM %s WHERE guild_id = ? AND role_id = ?", targetTable())
                .single(call().bind(guildId()).bind(role.getIdLong()))
                .update()
                .changed();
        if (result) {
            roleIds.remove(role.getIdLong());
        }
        return result;
    }

    /**
     * Returns a pretty string representation of the roles.
     *
     * @return the string representation of the roles
     */
    public String prettyString() {
        return roleIds.stream()
                      .map(id -> Optional.ofNullable(guild().getRoleById(id))
                                         .map(r -> "%s | %d".formatted(r.getName(), r.getPosition()))
                                         .orElse("Unkown (%d)".formatted(id)))
                      .collect(Collectors.joining(", "));
    }
}
