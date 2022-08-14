package de.chojo.repbot.commands.channel.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.access.guild.settings.sub.thanking.Channels;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.stream.Collectors;

public class List implements SlashHandler {
    private static final String MORE = String.format("$%s$", "command.channel.list.message.more");
    private final Guilds guilds;

    public List(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var channels = guilds.guild(event.getGuild()).settings().thanking().channels();
        event.replyEmbeds(getChannelList(channels, context)).queue();
    }

    private MessageEmbed getChannelList(Channels channels, EventContext context) {

        var channelNames = channels.channels().stream().map(IMentionable::getAsMention).limit(40).collect(Collectors.joining(", "));
        if (channels.channels().size() > 40) {
            channelNames += MORE;
        }

        var categoryNames = channels.categories().stream().map(IMentionable::getAsMention).limit(40).collect(Collectors.joining(", "));
        if (channels.categories().size() > 40) {
            categoryNames += MORE;
        }

        return new LocalizedEmbedBuilder(context.localizer())
                .setTitle(channels.isWhitelist() ? "command.channel.list.message.whitelist" : "command.channel.list.message.blacklist")
                .addField("words.channels", channelNames, false, Replacement.create("MORE", channels.channels().size() - 40))
                .addField("words.categories", categoryNames, false, Replacement.create("MORE", channels.channels().size() - 40))
                .build();
    }

}
