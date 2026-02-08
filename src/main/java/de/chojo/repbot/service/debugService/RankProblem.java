/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service.debugService;

import java.util.List;

public class RankProblem {
    long id;
    List<RanksProblemType> types;

    public long getId() {
        return id;
    }

    public List<RanksProblemType> getTypes() {
        return types;
    }

    public RankProblem(long id, List<RanksProblemType> types) {
        this.types = types;
        this.id = id;
    }
}
