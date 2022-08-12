package de.chojo.repbot.commands.repadmin.handler.reputation;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Remove implements SlashHandler {
    private final Guilds guilds;

    public Remove(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var user = event.getOption("user").getAsMember();
        var repUser = guilds.guild(event.getGuild()).reputation().user(user);
        var remove = event.getOption("remove").getAsLong();
        repUser.removeReputation(remove);
        event.reply(context.localize("command.repadmin.reputation.remove.message.removed",
                        Replacement.create("VALUE", remove), Replacement.createMention(user)))
                .setEphemeral(true).queue();

    }
}
