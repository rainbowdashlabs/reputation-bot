package de.chojo.repbot.commands.invite;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.config.Configuration;

public class Invite extends SlashCommand {
    public Invite(Configuration configuration) {
        super(Slash.of("invite", "command.invite.description")
                .command(new Handler(configuration)));
    }
}
