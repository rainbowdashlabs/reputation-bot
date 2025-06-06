/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot;


import de.chojo.repbot.core.Threading;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class TestScratch {
    @Test
    @Disabled
    void threadingException() throws InterruptedException {
        Threading threading = new Threading();

        threading.repBotWorker().submit(() ->{
            throw new  RuntimeException();
        });

        threading.repBotWorker().schedule(() ->{
            throw new  RuntimeException();
        }, 10, TimeUnit.SECONDS);

        Thread.sleep(15000);

        threading.repBotWorker().shutdown();
    }
}
