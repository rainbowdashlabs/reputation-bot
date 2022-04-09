package de.chojo.repbot.data.wrapper;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class GuildRanking {
    private final Supplier<Integer> pagecount;
    private final Function<Integer, List<ReputationUser>> pageSupplier;

    public GuildRanking(Supplier<Integer> pagecount, Function<Integer, List<ReputationUser>> pageSupplier) {
        this.pagecount = pagecount;
        this.pageSupplier = pageSupplier;
    }

    /**
     * The amount of pages in the raning
     * @return page amount
     */
    public int pages() {
        return pagecount.get();
    }

    /**
     * Get the page
     *
     * @param page page on zero based index
     * @return a list containing all entried for the page
     */
    public List<ReputationUser> page(int page) {
        return pageSupplier.apply(page);
    }
}
