package de.chojo.repbot.commands.channel.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.settings.sub.thanking.Channels;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Set;

public abstract class BaseChannelModifier implements SlashHandler {
    private static final java.util.Set<ChannelType> ALLOWED_CHANNEL =
            Set.of(ChannelType.TEXT, ChannelType.FORUM, ChannelType.CATEGORY, ChannelType.VOICE);
    private static final java.util.Set<ChannelType> TEXT_CHANNEL =
            Set.of(ChannelType.TEXT, ChannelType.FORUM, ChannelType.VOICE);
    private final Guilds guilds;

    public BaseChannelModifier(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var channels = guilds.guild(event.getGuild()).settings().thanking().channels();
        var channelType = event.getOption("channel").getChannelType();
        if (!ALLOWED_CHANNEL.contains(channelType)) {
            event.reply(context.localize("error.onlyTextOrCategory")).setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();

        var channel = event.getOption("channel").getAsChannel();
        if (TEXT_CHANNEL.contains(channelType)) {
            textChannel(event, context, channels, channel.asStandardGuildChannel());
        } else {
            category(event, context, channels, channel.asCategory());
        }
    }

    public abstract void textChannel(SlashCommandInteractionEvent event, EventContext context, Channels channels, StandardGuildChannel channel);

    public abstract void category(SlashCommandInteractionEvent event, EventContext context, Channels channels, Category category);
}
