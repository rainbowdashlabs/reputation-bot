/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.listener.voting;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.components.ActionComponent;


public record VoteComponent(Member member, ActionComponent component) {
}
