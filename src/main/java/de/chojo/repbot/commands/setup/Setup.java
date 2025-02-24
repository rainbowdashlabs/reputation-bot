/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.setup;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.setup.handler.Start;
import de.chojo.repbot.commands.thankwords.Thankwords;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.serialization.ThankwordsContainer;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Command for setting up the bot in a guild.
 */
public class Setup extends SlashCommand {
    private static final Logger log = getLogger(Setup.class);

    /**
     * Constructs a new Setup command.
     *
     * @param guilds the guilds provider
     * @param thankwordsContainer the container for thank words
     * @param configuration the bot configuration
     */
    public Setup(Guilds guilds, ThankwordsContainer thankwordsContainer, Configuration configuration) {
        super(Slash.of("setup", "command.setup.description")
                .guildOnly()
                .adminCommand()
                .command(new Start(guilds, thankwordsContainer, configuration)));
    }

    /**
     * Creates a new Setup command instance.
     *
     * @param guilds the guilds provider
     * @param configuration the bot configuration
     * @return a new Setup command instance
     */
    public static Setup of(Guilds guilds, Configuration configuration) {
        ThankwordsContainer thankwordsContainer;
        try {
            thankwordsContainer = new ObjectMapper()
                    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                    .readValue(Thankwords.class.getClassLoader().getResourceAsStream("Thankswords.json"),
                            ThankwordsContainer.class);
        } catch (IOException e) {
            thankwordsContainer = null;
            log.error("Could not read thankwords", e);
        }
        return new Setup(guilds, thankwordsContainer, configuration);
    }
}
