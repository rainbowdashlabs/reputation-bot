/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot;


import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class TestScratch {
    @Test
    void test() {
        Stream.of(List.of("a", "b", "c"), Collections.emptyList(), List.of("d", "e", "f"))
                .flatMap(List::stream)
                .forEach(System.out::println);
    }
}
