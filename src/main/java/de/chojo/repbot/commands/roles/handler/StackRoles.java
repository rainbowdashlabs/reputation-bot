package de.chojo.repbot.commands.roles.handler;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.Text;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.function.Consumer;

public class StackRoles extends BaseRoleModifier {

    public StackRoles(Refresh refresh, Guilds guilds) {
        super(refresh, guilds);
    }

    @Override
    public void modify(SlashCommandInteractionEvent event, EventContext context, Consumer<MessageEmbed> refresh) {
        var settings = guilds().guild(event.getGuild()).settings();
        if (event.getOptions().isEmpty()) {
            event.reply(Text.getBooleanMessage(context, settings.general().isStackRoles(),
                    "command.roles.stackroles.message.stacked", "command.roles.stackroles.message.notStacked")).queue();
            return;
        }
        var state = event.getOption("stack").getAsBoolean();

        if (settings.general().stackRoles(state)) {
            var menu = new LocalizedEmbedBuilder(context.guildLocalizer())
                    .setTitle(Text.getBooleanMessage(context, state,
                            "command.roles.stackroles.message.stacked", "command.roles.stackroles.message.notStacked"))
                    .build();
            refresh.accept(menu);
        }
    }
}
