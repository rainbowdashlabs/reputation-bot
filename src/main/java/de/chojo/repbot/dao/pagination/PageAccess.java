/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.pagination;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides access to paginated data.
 *
 * @param <T> the type of elements in the pages
 */
public class PageAccess<T> implements Iterable<List<T>> {
    private final Supplier<Integer> pagecount;
    private final Function<Integer, List<T>> pageSupplier;

    /**
     * Constructs a PageAccess object with the specified page count supplier and page supplier.
     *
     * @param pagecount the supplier for the total number of pages
     * @param pageSupplier the function to supply a page of data given a page number
     */
    public PageAccess(Supplier<Integer> pagecount, Function<Integer, List<T>> pageSupplier) {
        this.pagecount = pagecount;
        this.pageSupplier = pageSupplier;
    }

    /**
     * Returns the total number of pages.
     *
     * @return the total number of pages
     */
    public int pages() {
        return pagecount.get();
    }

    /**
     * Returns the data for the specified page.
     *
     * @param page the zero-based index of the page
     * @return a list containing all entries for the specified page
     */
    public List<T> page(int page) {
        return pageSupplier.apply(page);
    }

    /**
     * Returns an iterator over the pages of data.
     *
     * @return an iterator over the pages of data
     */
    @NotNull
    @Override
    public Iterator<List<T>> iterator() {
        return new Iterator<>() {
            private int currPage = 0;
            private final int pages = pages();

            /**
             * Returns true if there are more pages to iterate over.
             *
             * @return true if there are more pages to iterate over, false otherwise
             */
            @Override
            public boolean hasNext() {
                return currPage < pages;
            }

            /**
             * Returns the next page of data.
             *
             * @return the next page of data
             */
            @Override
            public List<T> next() {
                return page(currPage++);
            }
        };
    }
}
