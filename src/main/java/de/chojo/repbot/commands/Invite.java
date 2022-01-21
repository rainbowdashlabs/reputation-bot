package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.config.Configuration;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

public class Invite extends SimpleCommand {
    private final Localizer localizer;
    private final Configuration configuration;

    public Invite(Localizer localizer, Configuration configuration) {
        super("invite", null, "command.invite.description", (SimpleArgument[]) null, Permission.UNKNOWN);
        this.localizer = localizer;
        this.configuration = configuration;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        event.replyEmbeds(getResponse(event)).queue();
    }

    @NotNull
    private MessageEmbed getResponse(SlashCommandEvent event) {
        return new LocalizedEmbedBuilder(localizer, event)
                .setTitle("command.invite.title")
                .setDescription(localizer.localize("command.invite.click",
                        Replacement.create("URL", configuration.links().invite())))
                .build();
    }
}
