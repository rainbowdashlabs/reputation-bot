/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

public enum CooldownDirection {
    UNIDIRECTIONAL,
    BIDIRECTIONAL;

    public String localCode() {
        return "$%s$".formatted(getClass().getSimpleName().toLowerCase() + "." + this.name().toLowerCase());
    }
}
