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

public abstract class RolesHolder implements GuildHolder {
    private static final Logger log = getLogger(RolesHolder.class);
    protected final Set<Long> roleIds;
    protected final Thanking thanking;

    public RolesHolder(Thanking thanking, Set<Long> roleIds) {
        this.thanking = thanking;
        this.roleIds = roleIds;
    }

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

    public Set<Role> roles() {
        return roleIds.stream().map(guild()::getRoleById).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public Guild guild() {
        return thanking.guild();
    }

    protected abstract String targetTable();

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

    public String prettyString() {
        return roleIds.stream()
                      .map(id -> Optional.ofNullable(guild().getRoleById(id))
                                         .map(r -> "%s | %d".formatted(r.getName(), r.getPosition()))
                                         .orElse("Unkown (%d)".formatted(id)))
                      .collect(Collectors.joining(", "));
    }
}
