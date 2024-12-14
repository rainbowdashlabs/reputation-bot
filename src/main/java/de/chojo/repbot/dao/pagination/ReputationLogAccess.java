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

/**
 * Class for accessing paginated reputation log entries.
 */
public class ReputationLogAccess extends PageAccess<ReputationLogEntry> {

    /**
     * Constructs a new ReputationLogAccess.
     *
     * @param pagecount the supplier for the total number of pages
     * @param pageSupplier the function to supply a list of ReputationLogEntry for a given page number
     */
    public ReputationLogAccess(Supplier<Integer> pagecount, Function<Integer, List<ReputationLogEntry>> pageSupplier) {
        super(pagecount, pageSupplier);
    }
}
