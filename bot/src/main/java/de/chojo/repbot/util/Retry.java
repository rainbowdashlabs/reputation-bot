/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.util;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class Retry {
    public static <T> Optional<T> retryAndReturn(int tries, Callable<T> supplier, Consumer<Exception> onError) {
        int retries = 0;
        while (retries < tries) {
            retries++;
            try {
                return Optional.ofNullable(supplier.call());
            } catch (Exception e) {
                onError.accept(e);
            }
        }
        return Optional.empty();
    }

    public static boolean retryAndReturn(int tries, ThrowingRunnable run, Consumer<Exception> onError) {
        int retries = 0;
        while (retries < tries) {
            retries++;
            try {
                run.run();
                return true;
            } catch (Exception e) {
                onError.accept(e);
            }
        }
        return false;
    }
}
