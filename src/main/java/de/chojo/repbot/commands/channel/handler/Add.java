package de.chojo.repbot.commands.channel.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Add implements SlashHandler {
    private final Guilds guilds;

    public Add(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var channels = guilds.guild(event.getGuild()).settings().thanking().channels();
        var channelType = event.getOption("channel").getChannelType();
        if (channelType != ChannelType.TEXT && channelType != ChannelType.CATEGORY) {
            event.reply(context.localize("error.onlyTextOrCategory")).setEphemeral(true).queue();
            return;
        }
        if (channelType == ChannelType.TEXT) {
            var channel = event.getOption("channel").getAsChannel().asTextChannel();
            channels.add(channel);
            event.reply(
                    context.localize("command.channel.add.message.added",
                            Replacement.create("CHANNEL", channel.getAsMention()))).queue();
        } else {
            var channel = event.getOption("channel").getAsChannel().asCategory();
            channels.add(channel);
            event.reply(
                    context.localize("command.channel.add.message.added",
                            Replacement.create("CHANNEL", channel.getAsMention()))).queue();
        }
    }
}
