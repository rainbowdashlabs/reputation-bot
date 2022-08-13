package de.chojo.repbot.commands.top;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.commands.top.handler.Show;
import de.chojo.repbot.dao.provider.Guilds;

public class Top extends SlashCommand {
    public Top(Guilds guilds) {
        super(Slash.of("top", "command.reputation.description")
                .command(new Show(guilds))
                .argument(Argument.text("mode", "command.reputation.description.arg.mode").withAutoComplete()));
    }
}
