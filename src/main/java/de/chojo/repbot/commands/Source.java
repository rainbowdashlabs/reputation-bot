package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import net.dv8tion.jda.api.Permission;

public class Invite extends SimpleCommand {
    private final Localizer localizer;

    public Invite(Localizer localizer) {
        super("invite", null, "command.invite.description", null, null, Permission.UNKNOWN);
        this.localizer = localizer;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        eventWrapper.reply(new LocalizedEmbedBuilder(localizer, eventWrapper)
                .setTitle("command.invite.title")
                .setDescription(eventWrapper.localize("command.invite.click",
                        Replacement.create("URL", "https://discord.com/oauth2/authorize?client_id=834843896579489794&scope=bot&permissions=1342532672")))
                .build())
                .queue();
        return false;
    }
}
