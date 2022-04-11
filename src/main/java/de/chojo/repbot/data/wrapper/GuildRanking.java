package de.chojo.repbot.data.wrapper;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class GuildRanking extends PageAccess<ReputationUser> {
    public GuildRanking(Supplier<Integer> pagecount, Function<Integer, List<ReputationUser>> pageSupplier) {
        super(pagecount, pageSupplier);
    }
}
