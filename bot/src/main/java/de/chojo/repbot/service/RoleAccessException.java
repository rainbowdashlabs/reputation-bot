/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import net.dv8tion.jda.api.entities.Role;

public class RoleAccessException extends RuntimeException {
    private final Role role;

    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public RoleAccessException(Role role) {
        this.role = role;
    }

    public Role role() {
        return role;
    }
}
