package de.chojo.repbot.commands.roles.handler;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.dao.snapshots.ReputationRank;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.function.Consumer;

public class Remove extends BaseRoleModifier {

    public Remove(Refresh refresh, Guilds guilds) {
        super(refresh, guilds);
    }

    @Override
    public void modify(SlashCommandInteractionEvent event, EventContext context, Consumer<MessageEmbed> refresh) {
        var ranks = guilds().guild(event.getGuild()).settings().ranks();
        var role = event.getOption("role").getAsRole();

        if (ranks.rank(role).map(ReputationRank::remove).orElse(false)) {
            var menu = new LocalizedEmbedBuilder(context.guildLocalizer())
                    .setTitle("command.roles.remove.title.removed")
                    .setDescription("command.roles.remove.message.removed", Replacement.createMention("ROLE", role))
                    .build();
            refresh.accept(menu);
            return;
        }
        event.reply(context.localize("command.roles.remove.message.noreprole")).setEphemeral(true).queue();
    }
}
