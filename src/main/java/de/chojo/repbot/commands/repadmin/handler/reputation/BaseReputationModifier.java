package de.chojo.repbot.commands.repadmin.handler.reputation;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.reputation.sub.RepUser;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.service.RoleAssigner;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class BaseReputationModifier implements SlashHandler {

    private final RoleAssigner roleAssigner;
    private final Guilds guilds;

    public BaseReputationModifier(RoleAssigner roleAssigner, Guilds guilds) {
        this.roleAssigner = roleAssigner;
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var user = event.getOption("user").getAsMember();
        var repUser = guilds.guild(event.getGuild()).reputation().user(user);
        var add = event.getOption("amount").getAsLong();
        execute(event, context, user, repUser, add);
        roleAssigner.update(user);
    }

    abstract void execute(SlashCommandInteractionEvent event, EventContext context, Member user, RepUser repUser, long rep);
}
