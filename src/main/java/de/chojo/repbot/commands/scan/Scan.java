/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.commands.scan;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.commands.scan.handler.Cancel;
import de.chojo.repbot.commands.scan.handler.Start;
import de.chojo.repbot.commands.scan.util.Scanner;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Class representing the scan command for the bot.
 */
public class Scan implements SlashProvider<Slash> {

    private final Scanner scanner;

    /**
     * Constructs a new Scan command.
     *
     * @param guilds the guilds provider
     * @param configuration the bot configuration
     */
    public Scan(Guilds guilds, Configuration configuration) {
        scanner = new Scanner(guilds, configuration);
    }

    /**
     * Defines the slash command for scanning.
     *
     * @return the slash command
     */
    @Override
    public Slash slash() {
        return Slash.of("scan", "command.scan.description")
                .guildOnly()
                .adminCommand()
                .subCommand(SubCommand.of("start", "command.scan.start.description")
                        .handler(new Start(scanner))
                        .argument(Argument.channel("channel", "command.scan.start.options.channel.description"))
                        .argument(Argument.integer("numbermessages", "command.scan.start.options.numbermessages.description")
                                .min(1)
                                .max(10000)))
                .subCommand(SubCommand.of("cancel", "command.scan.cancel.description")
                        .handler(new Cancel(scanner)))
                .build();
    }

    /**
     * Initializes the scanner with the message analyzer.
     *
     * @param messageAnalyzer the message analyzer
     */
    public void lateInit(MessageAnalyzer messageAnalyzer) {
        scanner.lateInit(messageAnalyzer);
    }

    /**
     * Checks if the scanner is running for the given guild.
     *
     * @param guild the guild to check
     * @return true if the scanner is running, false otherwise
     */
    public boolean isRunning(Guild guild) {
        return scanner.isRunning(guild);
    }
}
