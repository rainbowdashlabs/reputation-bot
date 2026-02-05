/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import com.fasterxml.jackson.annotation.JsonSerializeAs;
import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.web.pojo.settings.sub.thanking.RolesHolderPOJO;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Set;

@JsonSerializeAs(RolesHolderPOJO.class)
public class DenyDonorRoles extends DenyRolesHolder {

    public DenyDonorRoles(Thanking thanking, Set<Long> roleIds) {
        super(thanking, roleIds);
    }

    @Override
    protected String targetTable() {
        return "deny_donor_roles";
    }
}
