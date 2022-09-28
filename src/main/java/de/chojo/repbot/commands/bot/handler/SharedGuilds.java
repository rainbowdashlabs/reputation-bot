package de.chojo.repbot.commands.bot.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.util.Guilds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.stream.Collectors;

public class SharedGuilds implements SlashHandler {
    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var userOpt = event.getOption("user");
        var userIdOpt = event.getOption("user_id");
        if (userOpt == null && userIdOpt == null) {
            event.reply("Provide a user").setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue();

        User user;

        if (userOpt != null) {
            user = userOpt.getAsUser();
        } else {
            try {
                user = event.getJDA().getShardManager().retrieveUserById(event.getIdLong()).complete();
            } catch (RuntimeException e) {
                event.getHook().editOriginal("Could not find this user.").queue();
                return;
            }
        }

        var mutualGuilds = user.getMutualGuilds();
        var guilds = mutualGuilds.stream().map(Guilds::prettyName).collect(Collectors.joining("\n"));

        event.getHook().editOriginalEmbeds(new EmbedBuilder().setTitle("Shared Guilds").setDescription(guilds).build())
             .queue();
    }
}
