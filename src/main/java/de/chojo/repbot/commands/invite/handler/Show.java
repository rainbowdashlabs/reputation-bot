package de.chojo.repbot.commands.invite.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class Show implements SlashHandler {
    private final Configuration configuration;

    public Show(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        event.replyEmbeds(getResponse(context)).queue();
    }

    @NotNull
    private MessageEmbed getResponse(EventContext context) {
        return new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.invite.message.title")
                .setDescription("command.invite.message.click", Replacement.create("URL", configuration.links().invite()))
                .build();
    }
}
