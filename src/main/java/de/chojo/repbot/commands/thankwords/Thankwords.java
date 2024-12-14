/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.thankwords;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.commands.thankwords.handler.Add;
import de.chojo.repbot.commands.thankwords.handler.Check;
import de.chojo.repbot.commands.thankwords.handler.List;
import de.chojo.repbot.commands.thankwords.handler.LoadDefault;
import de.chojo.repbot.commands.thankwords.handler.Remove;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.serialization.ThankwordsContainer;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Class representing the Thankwords slash command.
 */
public class Thankwords extends SlashCommand {

    /**
     * Logger instance for logging events.
     */
    private static final Logger log = getLogger(Thankwords.class);

    /**
     * Constructs a new Thankwords instance.
     *
     * @param messageAnalyzer the message analyzer
     * @param guilds the guilds provider
     * @param thankwordsContainer the thank words container
     */
    private Thankwords(MessageAnalyzer messageAnalyzer, Guilds guilds, ThankwordsContainer thankwordsContainer) {
        super(Slash.of("thankwords", "command.thankwords.description")
                .guildOnly()
                .adminCommand()
                .subCommand(SubCommand.of("add", "command.thankwords.add.description")
                        .handler(new Add(guilds))
                        .argument(Argument.text("pattern", "command.thankwords.add.options.pattern.description").asRequired()))
                .subCommand(SubCommand.of("remove", "command.thankwords.remove.description")
                        .handler(new Remove(guilds))
                        .argument(Argument.text("pattern", "command.thankwords.remove.options.pattern.description").asRequired()
                                          .withAutoComplete()))
                .subCommand(SubCommand.of("list", "command.thankwords.list.description")
                        .handler(new List(guilds)))
                .subCommand(SubCommand.of("check", "command.thankwords.check.description")
                        .handler(new Check(guilds, messageAnalyzer))
                        .argument(Argument.text("message", "command.thankwords.check.options.message.description")
                                          .asRequired()))
                .subCommand(SubCommand.of("loaddefault", "command.thankwords.loaddefault.description")
                        .handler(new LoadDefault(guilds, thankwordsContainer))
                        .argument(Argument.text("language", "command.thankwords.loaddefault.options.language.description")
                                          .withAutoComplete()))
        );
    }

    /**
     * Creates a new Thankwords instance.
     *
     * @param messageAnalyzer the message analyzer
     * @param guilds the guilds provider
     * @return a new Thankwords instance
     */
    public static Thankwords of(MessageAnalyzer messageAnalyzer, Guilds guilds) {
        ThankwordsContainer thankwordsContainer;
        try {
            thankwordsContainer = loadContainer();
        } catch (IOException e) {
            thankwordsContainer = null;
            log.error("Could not read thankwords", e);
        }
        return new Thankwords(messageAnalyzer, guilds, thankwordsContainer);
    }

    /**
     * Loads the ThankwordsContainer from a JSON file.
     *
     * @return the loaded ThankwordsContainer
     * @throws IOException if an I/O error occurs
     */
    public static ThankwordsContainer loadContainer() throws IOException {
        try (var input = Thankwords.class.getClassLoader().getResourceAsStream("Thankswords.json")) {
            return new ObjectMapper()
                    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                    .readValue(input, ThankwordsContainer.class);
        }
    }
}
