package de.chojo.repbot.web.pojo.settings.sub.thanking;

import java.util.Collections;
import java.util.Set;

public class RolesHolderPOJO {
    protected final Set<Long> roleIds;

    public RolesHolderPOJO(Set<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public Set<Long> roleIds() {
        return Collections.unmodifiableSet(roleIds);
    }
}
