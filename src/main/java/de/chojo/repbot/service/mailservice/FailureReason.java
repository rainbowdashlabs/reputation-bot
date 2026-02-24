/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.mailservice;

public enum FailureReason {
    /**
     * The email is already registered to another user.
     */
    ALREADY_REGISTERED,
    /**
     * The email has an invalid formal
     */
    INVALID_FORMAT,
    /**
     * The email that should be verified is connected to another user.
     */
    WRONG_USER,
    /**
     * The code provided for verification is invalid
     */
    INVALID_CODE,
    /**
     * The code provided is already expired.
     */
    CODE_EXPIRED,
    /**
     * The address that should be verified is unknown
     */
    UNKNOWN_ADDRESS
}
