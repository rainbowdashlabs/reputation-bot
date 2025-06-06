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

public class PageAccess<T> implements Iterable<List<T>> {
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

    /**
     * Returns the first page.
     * @return first page
     */
    public List<T> first(){
        return page(0);
    }

    @NotNull
    @Override
    public Iterator<List<T>> iterator() {
        return new Iterator<>() {
            private int currPage = 0;
            private final int pages = pages();

            @Override
            public boolean hasNext() {
                return currPage < pages;
            }

            @Override
            public List<T> next() {
                return page(currPage++);
            }
        };
    }
}
