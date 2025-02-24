/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import net.dv8tion.jda.api.entities.Role;

/**
 * Exception thrown when there is an issue accessing a role.
 */
public class RoleAccessException extends RuntimeException {
    /**
     * The role that caused the exception.
     */
    private final Role role;

    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param role the role that caused the exception
     */
    public RoleAccessException(Role role) {
        this.role = role;
    }

    /**
     * Returns the role that caused the exception.
     *
     * @return the role
     */
    public Role role() {
        return role;
    }
}
