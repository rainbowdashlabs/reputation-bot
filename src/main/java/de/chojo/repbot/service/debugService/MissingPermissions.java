/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.debugService;

import net.dv8tion.jda.api.Permission;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MissingPermissions {
    PermissionScope scope;
    long id;
    Set<Permission> permissions;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        MissingPermissions that = (MissingPermissions) o;
        return id == that.id && scope == that.scope;
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(scope);
        result = 31 * result + Long.hashCode(id);
        return result;
    }

    public PermissionScope getScope() {
        return scope;
    }

    public long getId() {
        return id;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public MissingPermissions(PermissionScope scope, long id, List<Permission> permissions) {
        this.scope = scope;
        this.id = id;
        this.permissions = new LinkedHashSet<>(permissions);
    }
}
