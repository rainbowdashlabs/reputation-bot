package de.chojo.repbot.commands.channel.handler.autopost;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.util.Premium;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.settings.sub.autopost.Autopost;
import de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshInterval;
import de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshType;
import de.chojo.repbot.dao.provider.GuildRepository;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

public class Enable implements SlashHandler {
    private final GuildRepository guildRepository;
    private final Configuration configuration;

    public Enable(GuildRepository guildRepository, Configuration configuration) {
        this.guildRepository = guildRepository;
        this.configuration = configuration;
    }

    //channel autopost enable <channel> [refresh_interval] [refresh_type]

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        if (Premium.isNotEntitled(event, configuration.skus().features().autopost().autopostChannel())) {
            Premium.replyPremium(event, context, configuration.skus().features().autopost().autopostChannel());
            return;
        }

        GuildChannelUnion channel = event.getOption("channel", OptionMapping::getAsChannel);
        if (channel.getType() != ChannelType.TEXT) {
            event.reply(context.localize("error.onlyTextChannel")).setEphemeral(true).queue();
            return;
        }
        Autopost autopost = guildRepository.guild(event.getGuild()).settings().autopost();
        autopost.active(true);
        TextChannel textChannel = channel.asTextChannel();
        autopost.channel(textChannel);

        if (event.getOption("refreshinterval") != null) {
            String refreshInterval = event.getOption("refreshinterval").getAsString();
            autopost.refreshInterval(RefreshInterval.valueOf(refreshInterval));
        }

        if (event.getOption("refreshtype") != null) {
            String refreshType = event.getOption("refreshtype").getAsString();
            autopost.refreshType(RefreshType.valueOf(refreshType));
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        if (event.getFocusedOption().getName().equals("refresh_interval")) {
            List<Command.Choice> complete = Completion.complete(event.getFocusedOption().getValue(), RefreshInterval.class);
            event.replyChoices(complete).queue();
        }
        if (event.getFocusedOption().getName().equals("refresh_type")) {
            List<Command.Choice> complete = Completion.complete(event.getFocusedOption().getValue(), RefreshType.class);
            event.replyChoices(complete).queue();
        }
    }
}
