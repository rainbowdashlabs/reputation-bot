package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.GdprData;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.sql.DataSource;

public class Gdpr extends SimpleCommand {
    private final GdprData gdprData;

    public Gdpr(DataSource dataSource) {
        super(CommandMeta.builder("gdpr", "command.gdpr.description")
                .addSubCommand("request", "command.gdpr.sub.request")
                .addSubCommand("delete", "command.gdpr.sub.delete"));
        gdprData = new GdprData(dataSource);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        if ("request".equalsIgnoreCase(event.getSubcommandName())) {
            var request = gdprData.request(event.getUser());
            if (request) {
                event.reply(context.localize("command.gdpr.sub.request.received")).setEphemeral(true).queue();
            } else {
                event.reply(context.localize("command.gdpr.sub.request.requested")).setEphemeral(true).queue();
            }
        }

        if ("delete".equalsIgnoreCase(event.getSubcommandName())) {
            var success = gdprData.queueUserDeletion(event.getUser());
            if (success) {
                event.reply(context.localize("command.gdpr.sub.delete.received")).setEphemeral(true).queue();
            } else {
                event.reply(context.localize("command.gdpr.sub.delete.scheduled")).setEphemeral(true).queue();
            }
        }
    }
}
