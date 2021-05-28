package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

public class Invite extends SimpleCommand {
    private final Localizer localizer;

    public Invite(Localizer localizer) {
        super("invite", null, "command.invite.description", (SimpleArgument[]) null, Permission.UNKNOWN);
        this.localizer = localizer;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        eventWrapper.reply(getResponse(eventWrapper))
                .queue();
        return true;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        var eventWrapper = MessageEventWrapper.create(event);
        event.reply(new MessageBuilder(getResponse(eventWrapper)).build()).queue();
    }

    @NotNull
    private MessageEmbed getResponse(MessageEventWrapper eventWrapper) {
        return new LocalizedEmbedBuilder(localizer, eventWrapper)
                .setTitle("command.invite.title")
                .setDescription(eventWrapper.localize("command.invite.click",
                        Replacement.create("URL", "https://discord.com/oauth2/authorize?client_id=834843896579489794&scope=bot&permissions=1342532672")))
                .build();
    }
}
