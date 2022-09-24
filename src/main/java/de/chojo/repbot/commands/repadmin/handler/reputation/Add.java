package de.chojo.repbot.commands.repadmin.handler.reputation;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Add implements SlashHandler {
    private final Guilds guilds;

    public Add(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var user = event.getOption("user").getAsMember();
        var repUser = guilds.guild(event.getGuild()).reputation().user(user);
        var add = event.getOption("amount").getAsLong();
        repUser.addReputation(add);
        event.reply(context.localize("command.repadmin.reputation.add.message.added",
                     Replacement.create("VALUE", add), Replacement.createMention(user)))
             .setEphemeral(true).queue();
    }
}
