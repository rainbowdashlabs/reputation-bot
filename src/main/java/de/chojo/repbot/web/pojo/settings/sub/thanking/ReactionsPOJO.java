/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub.thanking;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class ReactionsPOJO {
    protected final Set<String> reactions;
    protected String mainReaction;

    /**
     * All values are required. A partial body is rejected instead of silently resetting the omitted settings, because
     * the settings are applied by difference.
     */
    @JsonCreator
    public ReactionsPOJO(
            @JsonProperty(value = "reactions", required = true) Set<String> reactions,
            @JsonProperty(value = "mainReaction", required = true) String mainReaction) {
        this.reactions = reactions;
        this.mainReaction = mainReaction;
    }

    public String mainReaction() {
        return mainReaction;
    }

    public Set<String> reactions() {
        return reactions;
    }

    public Set<String> copyReactions() {
        return Set.copyOf(reactions);
    }
}
