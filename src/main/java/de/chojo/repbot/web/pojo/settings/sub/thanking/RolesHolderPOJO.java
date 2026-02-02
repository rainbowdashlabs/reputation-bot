/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub.thanking;

import java.util.Collections;
import java.util.Set;

public class RolesHolderPOJO {
    protected Set<Long> roleIds;

    public RolesHolderPOJO() {
    }

    public RolesHolderPOJO(Set<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public Set<Long> roleIds() {
        return Collections.unmodifiableSet(roleIds);
    }
}
