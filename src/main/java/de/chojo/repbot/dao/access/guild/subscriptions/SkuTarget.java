/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.subscriptions;

import net.dv8tion.jda.api.entities.Entitlement;

public enum SkuTarget {
    GUILD, USER;

    public static SkuTarget fromEntitlement(Entitlement entitlement) {
        return entitlement.getGuildId() == null ? USER : GUILD;
    }
}
