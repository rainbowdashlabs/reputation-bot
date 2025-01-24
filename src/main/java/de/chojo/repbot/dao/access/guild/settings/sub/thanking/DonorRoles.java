/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Set;

/**
 * Represents the donor roles in a guild.
 */
public class DonorRoles extends RolesHolder {
    private final Thanking thanking;

    /**
     * Constructs a new DonorRoles instance.
     *
     * @param thanking the thanking instance
     * @param roleIds  the set of role IDs
     */
    public DonorRoles(Thanking thanking, Set<Long> roleIds) {
        super(thanking, roleIds);
        this.thanking = thanking;
    }

    /**
     * Gets the guild associated with the donor roles.
     *
     * @return the guild
     */
    @Override
    public Guild guild() {
        return thanking.guild();
    }

    /**
     * Gets the ID of the guild associated with the donor roles.
     *
     * @return the guild ID
     */
    @Override
    public long guildId() {
        return thanking.guildId();
    }

    /**
     * Gets the target table name for the donor roles.
     *
     * @return the target table name
     */
    @Override
    protected String targetTable() {
        return "donor_roles";
    }
}
