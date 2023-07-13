/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.pagination;

import de.chojo.repbot.dao.snapshots.ReputationLogEntry;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ReputationLogAccess extends PageAccess<ReputationLogEntry> {
    public ReputationLogAccess(Supplier<Integer> pagecount, Function<Integer, List<ReputationLogEntry>> pageSupplier) {
        super(pagecount, pageSupplier);
    }
}
