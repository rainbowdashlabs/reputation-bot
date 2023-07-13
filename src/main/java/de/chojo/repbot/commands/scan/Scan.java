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

public class Scan implements SlashProvider<Slash> {

    private final Scanner scanner;

    public Scan(Guilds guilds, Configuration configuration) {
        scanner = new Scanner(guilds, configuration);
    }

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

    public void lateInit(MessageAnalyzer messageAnalyzer) {
        scanner.lateInit(messageAnalyzer);
    }

    public boolean isRunning(Guild guild) {
        return scanner.isRunning(guild);
    }
}
