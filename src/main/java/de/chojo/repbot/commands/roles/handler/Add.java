package de.chojo.repbot.commands.roles.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;

public class Add implements SlashHandler {
    private final Guilds guilds;

    public Add(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var role = event.getOption("role").getAsRole();
        var reputation = event.getOption("reputation").getAsLong();
        if (!event.getGuild().getSelfMember().canInteract(role)) {
            event.reply(context.localize("error.roleAccess",
                    Replacement.createMention(role))).setEphemeral(true).queue();
            return;
        }

        var ranks = guilds.guild(event.getGuild()).settings().ranks();
        ranks.add(role, reputation);
        event.reply(context.localize("command.roles.add.message.added",
                     Replacement.createMention("ROLE", role), Replacement.create("POINTS", reputation)))
             .mention(Collections.emptyList()).queue();
    }
}
