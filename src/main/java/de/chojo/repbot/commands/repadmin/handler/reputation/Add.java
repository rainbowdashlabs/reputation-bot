package de.chojo.repbot.commands.repadmin.handler.reputation;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.reputation.sub.RepUser;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.service.RoleAssigner;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Add extends BaseReputationModifier {

    public Add(RoleAssigner roleAssigner, Guilds guilds) {
        super(roleAssigner, guilds);
    }

    @Override
    void execute(SlashCommandInteractionEvent event, EventContext context, Member user, RepUser repUser, long rep) {
        repUser.addReputation(rep);
        event.reply(context.localize("command.repadmin.reputation.add.message.added",
                        Replacement.create("VALUE", rep), Replacement.createMention(user)))
                .setEphemeral(true).queue();
    }
}
