/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.pagination;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageAccessTest {
    PageAccess<Integer> access = new PageAccess<>(() -> 10, Collections::singletonList);

    @Test
    void pages() {
        assertEquals(10, access.pages());
    }

    @Test
    void page() {
        for (int i = 0; i < 10; i++) {
            assertEquals(Collections.singletonList(i), access.page(i));
        }
    }

    @Test
    void iterator() {
        int currPage = 0;
        for (var page : access) {
            assertEquals(Collections.singletonList(currPage++), page);
        }
        Assertions.assertEquals(currPage, 10);
    }
}
