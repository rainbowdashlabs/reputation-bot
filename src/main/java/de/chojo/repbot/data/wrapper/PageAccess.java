package de.chojo.repbot.data.wrapper;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class PageAccess<T> {
    private final Supplier<Integer> pagecount;
    private final Function<Integer, List<T>> pageSupplier;

    public PageAccess(Supplier<Integer> pagecount, Function<Integer, List<T>> pageSupplier) {
        this.pagecount = pagecount;
        this.pageSupplier = pageSupplier;
    }

    /**
     * The amount of pages which can be accessed.
     *
     * @return page amount
     */
    public int pages() {
        return pagecount.get();
    }

    /**
     * Get the page.
     *
     * @param page page on zero based index
     * @return a list containing all entries for the page
     */
    public List<T> page(int page) {
        return pageSupplier.apply(page);
    }
}
