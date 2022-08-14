package de.chojo.repbot.commands.roles.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class StackRoles implements SlashHandler {
    private final Guilds guilds;

    public StackRoles(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var settings = guilds.guild(event.getGuild()).settings();
        if (event.getOptions().isEmpty()) {
            event.reply(getBooleanMessage(context, settings.general().isStackRoles(),
                    "command.roles.stackroles.message.stacked", "command.roles.stackroles.message.notStacked")).queue();
            return;
        }
        var state = event.getOption("stack").getAsBoolean();

        if (settings.general().stackRoles(state)) {
            event.reply(getBooleanMessage(context, state,
                    "command.roles.stackroles.message.stacked", "command.roles.stackroles.message.notStacked")).queue();
        }
    }

    private String getBooleanMessage(EventContext context, boolean value, String whenTrue, String whenFalse) {
        return context.localize(value ? whenTrue : whenFalse);
    }
}
