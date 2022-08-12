package de.chojo.repbot.commands.roles.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;

public class Remove  implements SlashHandler {
    private final Guilds guilds;

    public Remove(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var ranks = guilds.guild(event.getGuild()).settings().ranks();
        var role = event.getOption("role").getAsRole();

        if (ranks.remove(role)) {
            event.reply(context.localize("command.roles.remove.message.removed",
                    Replacement.createMention("ROLE", role))).allowedMentions(Collections.emptyList()).queue();
            return;
        }
        event.reply(context.localize("command.roles.remove.message.noreprole")).setEphemeral(true).queue();
    }
}
