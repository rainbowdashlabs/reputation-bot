package de.chojo.repbot.commands.repsettings;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.repsettings.handler.EmojiInfo;
import de.chojo.repbot.commands.repsettings.handler.Info;
import de.chojo.repbot.dao.provider.Guilds;

public class RepSettings extends SlashCommand {

    public RepSettings(Guilds guilds) {
        super(Slash.of("repsettings", "command.repsettings.description")
                .guildOnly()
                .adminCommand()
                .subCommand(SubCommand.of("info", "command.repsettings.info.description")
                        .handler(new Info(guilds)))
                .subCommand(SubCommand.of("emojidebug", "command.repsettings.emojidebug.description")
                        .handler(new EmojiInfo(guilds))));
    }
}
