package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Gdpr extends SimpleCommand {
    private final de.chojo.repbot.dao.access.Gdpr gdpr;

    public Gdpr(de.chojo.repbot.dao.access.Gdpr gdpr) {
        super(CommandMeta.builder("gdpr", "command.gdpr.description")
                .addSubCommand("request", "command.gdpr.sub.request")
                .addSubCommand("delete", "command.gdpr.sub.delete"));
        this.gdpr = gdpr;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        if ("request".equalsIgnoreCase(event.getSubcommandName())) {
            var request = gdpr.request(event.getUser()).queueRequest();
            if (request) {
                event.reply(context.localize("command.gdpr.sub.request.received")).setEphemeral(true).queue();
            } else {
                event.reply(context.localize("command.gdpr.sub.request.requested")).setEphemeral(true).queue();
            }
        }

        if ("delete".equalsIgnoreCase(event.getSubcommandName())) {
            var success = gdpr.request(event.getUser()).queueDeletion();
            if (success) {
                event.reply(context.localize("command.gdpr.sub.delete.received")).setEphemeral(true).queue();
            } else {
                event.reply(context.localize("command.gdpr.sub.delete.scheduled")).setEphemeral(true).queue();
            }
        }
    }
}
