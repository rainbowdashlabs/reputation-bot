package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class GDPR extends SimpleCommand {
    protected GDPR() {
        super("gdpr",
                new String[]{"dsgvo"},
                "Request your data or its deletion",
                subCommandBuilder()
                        .add("request", "request a copy or your data.")
                        .add("delete", "Request deletion of your data.")
                        .build(),
                Permission.UNKNOWN);
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) {
            return false;
        }

        var cmd = context.argString(0).get();
        if ("request".equalsIgnoreCase(cmd)) {

            return true;
        }

        if ("delete".equalsIgnoreCase(cmd)) {

            return true;
        }

        return false;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {

    }
}
