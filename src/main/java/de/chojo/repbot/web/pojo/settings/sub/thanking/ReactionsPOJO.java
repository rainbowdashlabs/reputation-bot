/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub.thanking;

import java.util.Set;

public class ReactionsPOJO {
    protected final Set<String> reactions;
    protected String mainReaction;

    public ReactionsPOJO(Set<String> reactions, String mainReaction) {
        this.reactions = reactions;
        this.mainReaction = mainReaction;
    }

    public String mainReaction() {
        return mainReaction;
    }

    public Set<String> reactions() {
        return reactions;
    }
}
