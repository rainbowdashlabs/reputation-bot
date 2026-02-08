/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.debugService;

public class ReputationChannelProblem {
    long id;
    ReputationChannelProblemType type;

    public long getId() {
        return id;
    }

    public ReputationChannelProblemType getType() {
        return type;
    }

    public ReputationChannelProblem(long id, ReputationChannelProblemType type) {
        this.id = id;
        this.type = type;
    }
}
