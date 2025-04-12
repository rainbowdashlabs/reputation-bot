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
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.serialization.ThankwordsContainer;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class Setup extends SlashCommand {
    private static final Logger log = getLogger(Setup.class);

    public Setup(GuildRepository guildRepository, ThankwordsContainer thankwordsContainer, Configuration configuration) {
        super(Slash.of("setup", "command.setup.description")
                .guildOnly()
                .adminCommand()
                .command(new Start(guildRepository, thankwordsContainer, configuration)));
    }

    public static Setup of(GuildRepository guildRepository, Configuration configuration) {
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
        return new Setup(guildRepository, thankwordsContainer, configuration);
    }
}
