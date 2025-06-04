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
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.repbot.serialization.ThankwordsContainer;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class Thankwords extends SlashCommand {

    private static final Logger log = getLogger(Thankwords.class);

    private Thankwords(MessageAnalyzer messageAnalyzer, GuildRepository guildRepository, ThankwordsContainer thankwordsContainer) {
        super(Slash.of("thankwords", "command.thankwords.description")
                .guildOnly()
                .adminCommand()
                .subCommand(SubCommand.of("add", "command.thankwords.add.description")
                        .handler(new Add(guildRepository))
                        .argument(Argument.text("pattern", "command.thankwords.add.options.pattern.description").asRequired().maxLength(25)))
                .subCommand(SubCommand.of("remove", "command.thankwords.remove.description")
                        .handler(new Remove(guildRepository))
                        .argument(Argument.text("pattern", "command.thankwords.remove.options.pattern.description").asRequired()
                                          .withAutoComplete()))
                .subCommand(SubCommand.of("list", "command.thankwords.list.description")
                        .handler(new List(guildRepository)))
                .subCommand(SubCommand.of("check", "command.thankwords.check.description")
                        .handler(new Check(guildRepository, messageAnalyzer))
                        .argument(Argument.text("message", "command.thankwords.check.options.message.description")
                                          .asRequired()))
                .subCommand(SubCommand.of("loaddefault", "command.thankwords.loaddefault.description")
                        .handler(new LoadDefault(guildRepository, thankwordsContainer))
                        .argument(Argument.text("language", "command.thankwords.loaddefault.options.language.description")
                                          .withAutoComplete()))
        );
    }

    public static Thankwords of(MessageAnalyzer messageAnalyzer, GuildRepository guildRepository) {
        ThankwordsContainer thankwordsContainer;
        try {
            thankwordsContainer = loadContainer();
        } catch (IOException e) {
            thankwordsContainer = null;
            log.error("Could not read thankwords", e);
        }
        return new Thankwords(messageAnalyzer, guildRepository, thankwordsContainer);
    }

    public static ThankwordsContainer loadContainer() throws IOException {
        try (var input = Thankwords.class.getClassLoader().getResourceAsStream("Thankswords.json")) {
            return new ObjectMapper()
                    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                    .readValue(input, ThankwordsContainer.class);
        }
    }
}
