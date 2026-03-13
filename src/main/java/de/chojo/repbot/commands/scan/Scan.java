/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.scan;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.repbot.commands.scan.handler.Start;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.service.ScanService;

public class Scan implements SlashProvider<Slash> {

    private final ScanService service;
    private final Configuration configuration;

    public Scan(ScanService service, Configuration configuration) {
        this.service = service;
        this.configuration = configuration;
    }

    @Override
    public Slash slash() {
        return Slash.of("scan", "command.scan.description")
                    .guildOnly()
                    .adminCommand()
                    .command(new Start(service, configuration))
                    .build();
    }
}
