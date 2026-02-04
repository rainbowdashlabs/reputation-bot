/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.service;

import de.chojo.repbot.core.Data;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.slf4j.LoggerFactory.getLogger;

public class MonitorService extends ListenerAdapter {
    private static final Logger log = getLogger(MonitorService.class);
    private final Data data;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public MonitorService(Data data) {
        this.data = data;
    }

    @Override
    public void onGenericInteractionCreate(@NotNull GenericInteractionCreateEvent event) {
        data.metrics().service().countInteraction();
        executorService.schedule(() -> check(event), 10, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void check(GenericInteractionCreateEvent event) {
        if (event.isAcknowledged()) {
            data.metrics().service().successfulInteraction();
            return;
        }
        data.metrics().service().failedInteraction();
        log.error("Interaction not acknowledged after 10 seconds. {}", event);

    }
}
