package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.config.Configuration;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class Invite extends SimpleCommand {
    private final Configuration configuration;

    public Invite(Configuration configuration) {
        super(CommandMeta.builder("invite", "command.invite.description"));
        this.configuration = configuration;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        event.replyEmbeds(getResponse(context)).queue();
    }

    @NotNull
    private MessageEmbed getResponse(SlashCommandContext context) {
        return new LocalizedEmbedBuilder(context.localizer())
                .setTitle("command.invite.title")
                .setDescription("command.invite.click", Replacement.create("URL", configuration.links().invite()))
                .build();
    }
}
