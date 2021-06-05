package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.data.GuildData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Channel extends SimpleCommand {
    private final GuildData data;
    private final Localizer loc;

    public Channel(DataSource dataSource, Localizer loc) {
        super("channel",
                null,
                "command.channel.description",
                subCommandBuilder()
                        .add("set", "command.channel.sub.set", argsBuilder()
                                .add(OptionType.CHANNEL, "channel", "channel", true)
                                .build()
                        )
                        .add("add", "command.channel.sub.add", argsBuilder()
                                .add(OptionType.CHANNEL, "channel", "channel", true)
                                .build()
                        )
                        .add("remove", "command.channel.sub.remove", argsBuilder()
                                .add(OptionType.CHANNEL, "channel", "channel", true)
                                .build()
                        )
                        .add("list", "command.channel.sub.list")
                        .build(),
                Permission.MANAGE_SERVER);
        data = new GuildData(dataSource);
        this.loc = loc;
    }

    @Override
    public boolean onCommand(MessageEventWrapper messageEventWrapper, CommandContext commandContext) {
        var optsubCmd = commandContext.argString(0);
        if (optsubCmd.isEmpty()) return false;
        var subCmd = optsubCmd.get();

        if ("set".equalsIgnoreCase(subCmd)) {
            return set(messageEventWrapper, commandContext.subContext(subCmd));
        }
        if ("remove".equalsIgnoreCase(subCmd)) {
            return remove(messageEventWrapper, commandContext.subContext(subCmd));
        }
        if ("add".equalsIgnoreCase(subCmd)) {
            return add(messageEventWrapper, commandContext.subContext(subCmd));
        }
        if ("list".equalsIgnoreCase(subCmd)) {
            return list(messageEventWrapper);
        }
        return false;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        var subCmd = event.getSubcommandName();
        if ("set".equalsIgnoreCase(subCmd)) {
            set(event);
        }
        if ("remove".equalsIgnoreCase(subCmd)) {
            remove(event);
        }
        if ("add".equalsIgnoreCase(subCmd)) {
            add(event);
        }
        if ("list".equalsIgnoreCase(subCmd)) {
            list(event);
        }
    }

    private void add(SlashCommandEvent event) {
        var channel = event.getOption("channel").getAsMessageChannel();
        if (channel == null || channel.getType() != ChannelType.TEXT) {
            event.reply(loc.localize("error.invalidChannel")).setEphemeral(true).queue();
            return;
        }

        data.addChannel(event.getGuild(), channel);
        event.reply(
                loc.localize("command.channel.sub.add.added", event.getGuild(),
                        Replacement.create("CHANNEL", ((TextChannel) channel).getAsMention()))).queue();
    }

    private boolean add(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;

        var args = context.args();
        var validTextChannels = DiscordResolver.getValidTextChannels(eventWrapper.getGuild(), args);
        if (validTextChannels.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.invalidChannel"), 10);
            return true;
        }

        var addedChannel = validTextChannels.stream()
                .filter(c -> data.addChannel(eventWrapper.getGuild(), c))
                .map(IMentionable::getAsMention)
                .collect(Collectors.joining(", "));
        eventWrapper.reply(
                eventWrapper.localize("command.channel.sub.add.added",
                        Replacement.create("CHANNEL", addedChannel))).queue();
        return true;
    }

    private void remove(SlashCommandEvent event) {
        var channel = event.getOption("channel").getAsMessageChannel();
        if (channel == null || channel.getType() != ChannelType.TEXT) {
            event.reply(loc.localize("error.invalidChannel")).setEphemeral(true).queue();
            return;
        }
        data.deleteChannel(event.getGuild(), channel);

        event.reply(loc.localize("command.channel.sub.remove.removed",
                Replacement.create("CHANNEL", ((TextChannel) channel).getAsMention()))).queue();
    }

    private boolean remove(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;

        var args = context.args();
        var validTextChannels = DiscordResolver.getValidTextChannels(eventWrapper.getGuild(), args);
        if (validTextChannels.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.invalidChannel"), 10);
            return true;
        }

        var removedChannel = validTextChannels.stream()
                .filter(c -> data.deleteChannel(eventWrapper.getGuild(), c))
                .map(IMentionable::getAsMention)
                .collect(Collectors.joining(", "));
        eventWrapper.reply(eventWrapper.localize("command.channel.sub.remove.removed",
                Replacement.create("CHANNEL", removedChannel))).queue();
        return true;
    }

    private void list(SlashCommandEvent event) {
        var guildSettings = data.getGuildSettings(event.getGuild());
        if (guildSettings.isEmpty()) return;

        var settings = guildSettings.get();
        var channelNames = DiscordResolver
                .getValidTextChannelsById(
                        event.getGuild(), new ArrayList<>(settings.getActiveChannel()))
                .stream().map(IMentionable::getAsMention).collect(Collectors.joining(", "));
        event.reply(loc.localize("command.channel.sub.list.list",
                Replacement.create("CHANNEL", channelNames))).queue();
    }

    private boolean list(MessageEventWrapper eventWrapper) {
        var guildSettings = data.getGuildSettings(eventWrapper.getGuild());
        if (guildSettings.isEmpty()) return true;

        var settings = guildSettings.get();
        var channelNames = DiscordResolver
                .getValidTextChannelsById(
                        eventWrapper.getGuild(), new ArrayList<>(settings.getActiveChannel()))
                .stream().map(IMentionable::getAsMention).collect(Collectors.joining(", "));
        eventWrapper.reply(eventWrapper.localize("command.channel.sub.list.list",
                Replacement.create("CHANNEL", channelNames))).queue();
        return true;
    }

    private void set(SlashCommandEvent event) {
        var channel = event.getOption("channel").getAsMessageChannel();
        if (channel == null || channel.getType() != ChannelType.TEXT) {
            event.reply(loc.localize("error.invalidChannel")).setEphemeral(true).queue();
            return;
        }

        data.clearChannel(event.getGuild());
        data.addChannel(event.getGuild(), channel);
        event.reply(loc.localize("command.channel.sub.set.set",
                Replacement.create("CHANNEL", ((TextChannel) channel).getAsMention()))).queue();
    }

    private boolean set(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;

        var args = context.args();
        var validTextChannels = DiscordResolver.getValidTextChannels(eventWrapper.getGuild(), args);
        if (validTextChannels.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.invalidChannel"), 10);
            return true;
        }

        data.clearChannel(eventWrapper.getGuild());
        var collect = validTextChannels.stream()
                .filter(c -> data.addChannel(eventWrapper.getGuild(), c))
                .map(IMentionable::getAsMention)
                .collect(Collectors.joining(", "));
        eventWrapper.reply(eventWrapper.localize("command.channel.sub.set.set",
                Replacement.create("CHANNEL", collect))).queue();
        return true;
    }
}
