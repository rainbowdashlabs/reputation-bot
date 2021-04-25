package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import net.dv8tion.jda.api.Permission;

public class Source extends SimpleCommand {
    private final Localizer localizer;

    public Source(Localizer localizer) {
        super("source", null, "command.source.description", null, null, Permission.UNKNOWN);
        this.localizer = localizer;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        eventWrapper.reply(new LocalizedEmbedBuilder(localizer, eventWrapper)
                .setTitle("command.source.title")
                .setDescription(eventWrapper.localize("command.source.click",
                        Replacement.create("URL", "https://github.com/RainbowDashLabs/reputation-bot")))
                .build())
                .queue();
        return true;
    }
}
