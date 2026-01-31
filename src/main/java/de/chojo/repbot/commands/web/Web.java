package de.chojo.repbot.commands.web;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.repbot.web.sessions.SessionService;

public class Web extends SlashCommand {
    public Web(SessionService sessionService) {
        super(Slash.of("web", "command.web.description")
                   .command((event, ctx) -> {
                       String guildSession = sessionService.createGuildSession(event.getGuild(), event.getMember());
                       event.reply(guildSession).queue();
                   }));
    }
}
