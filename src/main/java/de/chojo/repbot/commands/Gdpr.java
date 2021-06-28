package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.GdprData;
import de.chojo.repbot.service.GdprReporterService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import javax.sql.DataSource;

public class Gdpr extends SimpleCommand {
    private final GdprData gdprData;
    private final ILocalizer localizer;

    public Gdpr(DataSource dataSource, ILocalizer localizer) {
        super("gdpr",
                new String[]{"dsgvo"},
                "command.gdpr.description",
                subCommandBuilder()
                        .add("request", "command.gdpr.sub.request")
                        .add("delete", "command.gdpr.sub.delete")
                        .build(),
                Permission.UNKNOWN);
        this.localizer = localizer;
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
                eventWrapper.reply(localizer.localize("command.gdpr.sub.request.received", eventWrapper.getGuild())).queue();
            } else {
                eventWrapper.reply(localizer.localize("command.gdpr.sub.request.requested", eventWrapper.getGuild())).queue();
            }
            return true;
        }

        if ("delete".equalsIgnoreCase(cmd)) {
            var success = gdprData.queueUserDeletion(eventWrapper.getAuthor());
            if (success) {
                eventWrapper.reply(localizer.localize("command.gdpr.sub.delete.received", eventWrapper.getGuild())).queue();
            } else {
                eventWrapper.reply(localizer.localize("command.gdpr.sub.delete.scheduled", eventWrapper.getGuild())).queue();
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
                event.reply(localizer.localize("command.gdpr.sub.request.received", event.getGuild())).setEphemeral(true).queue();
            } else {
                event.reply(localizer.localize("command.gdpr.sub.request.requested", event.getGuild())).setEphemeral(true).queue();
            }
        }

        if ("delete".equalsIgnoreCase(event.getSubcommandName())) {
            var success = gdprData.queueUserDeletion(event.getUser());
            if (success) {
                event.reply(localizer.localize("command.gdpr.sub.delete.received", event.getGuild())).setEphemeral(true).queue();
            } else {
                event.reply(localizer.localize("command.gdpr.sub.delete.scheduled", event.getGuild())).setEphemeral(true).queue();
            }
        }
    }
}
