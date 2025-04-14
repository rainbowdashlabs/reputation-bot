/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.listener.voting;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.components.ActionComponent;

/**
 * Record representing a vote component.
 *
 * @param member the member associated with the vote component
 * @param component the action component for the vote
 */
public record VoteComponent(Member member, ActionComponent component) {
}
