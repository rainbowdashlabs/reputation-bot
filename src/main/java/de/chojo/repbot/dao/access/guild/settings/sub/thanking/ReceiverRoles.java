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
 * Manages the roles for receivers in a guild.
 */
public class ReceiverRoles extends RolesHolder {
    private final Thanking thanking;

    /**
     * Constructs a ReceiverRoles instance.
     *
     * @param thanking the thanking settings
     * @param roleIds the set of role IDs
     */
    public ReceiverRoles(Thanking thanking, Set<Long> roleIds) {
        super(thanking, roleIds);
        this.thanking = thanking;
    }

    /**
     * Gets the guild associated with the receiver roles.
     *
     * @return the guild
     */
    @Override
    public Guild guild() {
        return thanking.guild();
    }

    /**
     * Gets the guild ID associated with the receiver roles.
     *
     * @return the guild ID
     */
    @Override
    public long guildId() {
        return thanking.guildId();
    }

    /**
     * Gets the target table name for receiver roles.
     *
     * @return the target table name
     */
    @Override
    protected String targetTable() {
        return "receiver_roles";
    }
}
