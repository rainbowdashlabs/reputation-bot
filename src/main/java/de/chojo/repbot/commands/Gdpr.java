package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.GdprData;
import de.chojo.repbot.service.GdprReporterService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import javax.sql.DataSource;

public class Gdpr extends SimpleCommand {
    private final GdprReporterService reporterService;
    private final GdprData gdprData;

    public Gdpr(GdprReporterService reporterService, DataSource dataSource) {
        super("gdpr",
                new String[]{"dsgvo"},
                "Request your data or its deletion",
                subCommandBuilder()
                        .add("request", "request a copy or your data.")
                        .add("delete", "Request deletion of your data.")
                        .build(),
                Permission.UNKNOWN);
        this.reporterService = reporterService;
        gdprData = new GdprData(dataSource);
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) {
            return false;
        }

        var cmd = context.argString(0).get();
        if ("request".equalsIgnoreCase(cmd)) {
            var request = gdprData.request(eventWrapper.getAuthor());
            if (request) {
                eventWrapper.reply("We received your request. You will get your data within the next hours. You can request your data every 30 days.").queue();
            } else {
                eventWrapper.reply("Your have requested your data in the last 30 days. You can request your data only every 30 days.").queue();
            }
            return true;
        }

        if ("delete".equalsIgnoreCase(cmd)) {
            var success = gdprData.queueUserDeletion(eventWrapper.getAuthor());
            if (success) {
                eventWrapper.reply("Your data is scheduled to be deleted. It will be deleted within the next days.").queue();
            } else {
                eventWrapper.reply("Your data is already scheduled to be deleted. It will be deleted within the next days.").queue();
            }
            return true;
        }

        return false;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        if ("request".equalsIgnoreCase(event.getSubcommandName())) {
            var request = gdprData.request(event.getUser());
            if (request) {
                event.reply("We received your request. You will get your data within the next hours. You can request your data every 30 days.").setEphemeral(true).queue();
            } else {
                event.reply("Your have requested your data in the last 30 days. You can request your data only every 30 days.").setEphemeral(true).queue();
            }
        }

        if ("delete".equalsIgnoreCase(event.getSubcommandName())) {
            var success = gdprData.queueUserDeletion(event.getUser());
            if (success) {
                event.reply("Your data is scheduled to be deleted. It will be deleted within the next days.").setEphemeral(true).queue();
            } else {
                event.reply("Your data is already scheduled to be deleted. It will be deleted within the next days.").setEphemeral(true).queue();
            }
        }
    }
}
