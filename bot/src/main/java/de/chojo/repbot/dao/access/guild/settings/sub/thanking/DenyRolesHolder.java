/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import com.fasterxml.jackson.annotation.JsonSerializeAs;
import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.web.pojo.settings.sub.thanking.RolesHolderPOJO;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

@JsonSerializeAs(RolesHolderPOJO.class)
public abstract class DenyRolesHolder extends RolesHolder {
    private static final Logger log = getLogger(DenyRolesHolder.class);

    public DenyRolesHolder(Thanking thanking, Set<Long> roleIds) {
        super(thanking, roleIds);
    }

    @Override
    public boolean hasRole(@Nullable Member member) {
        if (member == null) {
            log.trace("Member is null. Could not determine group.");
            return false;
        }
        if (roleIds.isEmpty()) return false;
        for (var role : member.getRoles()) {
            if (roleIds.contains(role.getIdLong())) return true;
        }
        return false;
    }
}
