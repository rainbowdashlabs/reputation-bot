package de.chojo.repbot.data.wrapper;

import de.chojo.repbot.dao.snapshots.ReputationLogEntry;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ReputationLogAccess extends PageAccess<ReputationLogEntry> {
    public ReputationLogAccess(Supplier<Integer> pagecount, Function<Integer, List<ReputationLogEntry>> pageSupplier) {
        super(pagecount, pageSupplier);
    }
}
