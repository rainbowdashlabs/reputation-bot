/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.info;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.info.handler.Show;
import de.chojo.repbot.config.Configuration;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Command class for the info command.
 */
public class Info extends SlashCommand {
    private static final Logger log = getLogger(Info.class);

    /**
     * Constructs a new Info command.
     *
     * @param version the version of the application
     * @param configuration the configuration of the application
     */
    private Info(String version, Configuration configuration) {
        super(Slash.of("info", "command.info.description")
                .command(new Show(version, configuration)));
    }

    /**
     * Creates a new Info command instance.
     *
     * @param configuration the configuration of the application
     * @return a new Info command instance
     */
    public static Info create(Configuration configuration) {
        var version = "undefined";
        try (var input = Info.class.getClassLoader().getResourceAsStream("version")) {
            version = new String(input.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            log.error("Could not determine version.");
        }
        return new Info(version, configuration);
    }
}
