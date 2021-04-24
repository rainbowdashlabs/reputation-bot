package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.data.GuildData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IMentionable;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Channel extends SimpleCommand {
    private final GuildData data;

    public Channel(DataSource dataSource, Localizer localizer) {
        super("channel",
                null,
                "Manage channel for reputation collection.",
                null,
                subCommandBuilder()
                        .add("set", "<channel...>", "Set the reputation channel.")
                        .add("add", "<channel...>", "Add a reputation channel.")
                        .add("remove", "<channel...>", "Remove a reputation channel.")
                        .add("list", null, "List reputation channel.")
                        .build(),
                Permission.ADMINISTRATOR);
        data = new GuildData(dataSource);
    }

    @Override
    public boolean onCommand(MessageEventWrapper messageEventWrapper, CommandContext commandContext) {
        var optsubCmd = commandContext.argString(0);
        if (optsubCmd.isEmpty()) return false;
        var subCmd = optsubCmd.get();

        if ("set".equalsIgnoreCase(subCmd)) {
            return set(messageEventWrapper, commandContext.subCommandcontext(subCmd));
        }
        if ("remove".equalsIgnoreCase(subCmd)) {
            return remove(messageEventWrapper, commandContext.subCommandcontext(subCmd));
        }
        if ("add".equalsIgnoreCase(subCmd)) {
            return add(messageEventWrapper, commandContext.subCommandcontext(subCmd));
        }
        if ("list".equalsIgnoreCase(subCmd)) {
            return list(messageEventWrapper, commandContext.subCommandcontext(subCmd));
        }
        return false;
    }

    private boolean add(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;

        var args = context.args();
        var validTextChannels = DiscordResolver.getValidTextChannels(eventWrapper.getGuild(), args);
        if (validTextChannels.isEmpty()) {
            eventWrapper.replyNonMention("No valid channels provided").queue();
            return true;
        }

        var addedChannel = validTextChannels.stream()
                .filter(c -> data.addChannel(eventWrapper.getGuild(), c))
                .map(IMentionable::getAsMention)
                .collect(Collectors.joining(", "));
        eventWrapper.replyNonMention("Added the following channel to reputation channel:\n" + addedChannel).queue();
        return true;
    }

    private boolean remove(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;

        var args = context.args();
        var validTextChannels = DiscordResolver.getValidTextChannels(eventWrapper.getGuild(), args);
        if (validTextChannels.isEmpty()) {
            eventWrapper.replyNonMention("No valid channels provided").queue();
            return true;
        }

        var removedChannel = validTextChannels.stream()
                .filter(c -> data.deleteChannel(eventWrapper.getGuild(), c))
                .map(IMentionable::getAsMention)
                .collect(Collectors.joining(", "));
        eventWrapper.replyNonMention("Removed the following channel from reputation channel:\n" + removedChannel).queue();
        return true;
    }

    private boolean list(MessageEventWrapper eventWrapper, CommandContext context) {
        var guildSettings = data.getGuildSettings(eventWrapper.getGuild());
        if (guildSettings.isEmpty()) return true;

        var settings = guildSettings.get();
        var channelNames = DiscordResolver
                .getValidTextChannelsById(
                        eventWrapper.getGuild(), new ArrayList<>(settings.getActiveChannel()))
                .stream().map(IMentionable::getAsMention).collect(Collectors.joining(", "));
        eventWrapper.replyNonMention("Following channel are channels where reputation can be collected:\n" + channelNames)
                .queue();
        return true;
    }

    private boolean set(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) return false;

        var args = context.args();
        var validTextChannels = DiscordResolver.getValidTextChannels(eventWrapper.getGuild(), args);
        if (validTextChannels.isEmpty()) {
            eventWrapper.replyNonMention("No valid channels provided").queue();
            return true;
        }

        data.clearChannel(eventWrapper.getGuild());
        var collect = validTextChannels.stream()
                .filter(c -> data.addChannel(eventWrapper.getGuild(), c))
                .map(IMentionable::getAsMention)
                .collect(Collectors.joining(", "));
        eventWrapper.replyNonMention("Set the following channel as reputation channel:\n" + collect).queue();
        return true;
    }
}
