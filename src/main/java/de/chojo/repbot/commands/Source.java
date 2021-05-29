package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

public class Source extends SimpleCommand {
    private final Localizer localizer;

    public Source(Localizer localizer) {
        super("source", null, "command.source.description", (SimpleArgument[]) null, Permission.UNKNOWN);
        this.localizer = localizer;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        eventWrapper.reply(getResponse(eventWrapper))
                .queue();
        return true;
    }

    @NotNull
    private MessageEmbed getResponse(MessageEventWrapper eventWrapper) {
        return new LocalizedEmbedBuilder(localizer, eventWrapper)
                .setTitle("command.source.title")
                .setDescription(localizer.localize("command.source.click",
                        Replacement.create("URL", "https://github.com/RainbowDashLabs/reputation-bot")))
                .build();
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        var eventWrapper = MessageEventWrapper.create(event);
        event.reply(wrap(getResponse(eventWrapper))).queue();
    }
}
