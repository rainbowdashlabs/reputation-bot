package de.chojo.repbot.commands.gdpr.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.Gdpr;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Request implements SlashHandler {
    private final Gdpr gdpr;

    public Request(Gdpr gdpr) {
        this.gdpr = gdpr;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var request = gdpr.request(event.getUser()).queueRequest();
        if (request) {
            event.reply(context.localize("command.gdpr.sub.request.received")).setEphemeral(true).queue();
        } else {
            event.reply(context.localize("command.gdpr.sub.request.requested")).setEphemeral(true).queue();
        }
    }
}
