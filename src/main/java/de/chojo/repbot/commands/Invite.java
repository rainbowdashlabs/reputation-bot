package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
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
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        eventWrapper.reply(getResponse(eventWrapper))
                .queue();
        return true;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommandContext context) {
        var eventWrapper = MessageEventWrapper.create(event);
        event.replyEmbeds(getResponse(eventWrapper)).queue();
    }

    @NotNull
    private MessageEmbed getResponse(MessageEventWrapper eventWrapper) {
        return new LocalizedEmbedBuilder(localizer, eventWrapper)
                .setTitle("command.invite.title")
                .setDescription(localizer.localize("command.invite.click",
                        Replacement.create("URL", configuration.links().invite())))
                .build();
    }
}
