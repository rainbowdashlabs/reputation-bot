/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.services.userservice;

/**
 * Additional profile data available to guild admins, mirroring the repadmin profile command.
 *
 * @param rawReputation the user's raw reputation before offsets
 * @param repOffset     the reputation offset applied to the user
 * @param donated       the total reputation donated by the user
 */
public record AdminProfile(long rawReputation, long repOffset, long donated) {}
