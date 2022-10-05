package de.chojo.repbot.commands.roles.handler;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.function.Consumer;

public class Add extends BaseRoleModifier {

    public Add(Refresh refresh, Guilds guilds) {
        super(refresh, guilds);
    }

    @Override
    public void modify(SlashCommandInteractionEvent event, EventContext context, Consumer<MessageEmbed> refresh) {
        var role = event.getOption("role").getAsRole();
        var reputation = event.getOption("reputation").getAsLong();
        if (!event.getGuild().getSelfMember().canInteract(role)) {
            event.reply(context.localize("error.roleAccess",
                    Replacement.createMention(role))).setEphemeral(true).queue();
            return;
        }

        var ranks = guilds().guild(event.getGuild()).settings().ranks();
        ranks.add(role, reputation);
        var menu = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.roles.add.title.added")
                .setDescription("command.roles.add.message.added", Replacement.createMention("ROLE", role), Replacement.create("POINTS", reputation))
                .build();
        refresh.accept(menu);
    }
}
