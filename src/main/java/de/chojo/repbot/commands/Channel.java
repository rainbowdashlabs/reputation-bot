package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.dao.access.guild.settings.sub.thanking.Channels;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.FilterUtil;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.stream.Collectors;

public class Channel extends SimpleCommand {
    private final Guilds guilds;

    public Channel(Guilds guilds) {
        super(CommandMeta.builder("channel", "command.channel.description")
                .addSubCommand("set", "command.channel.sub.set", argsBuilder()
                        .add(SimpleArgument.channel("channel", "command.channel.sub.set.arg.channel").asRequired()))
                .addSubCommand("add", "command.channel.sub.add", argsBuilder()
                        .add(SimpleArgument.channel("channel", "command.channel.sub.add.arg.channel").asRequired()))
                .addSubCommand("addall", "command.channel.sub.addAll")
                .addSubCommand("remove", "command.channel.sub.remove", argsBuilder()
                        .add(SimpleArgument.channel("channel", "command.channel.sub.remove.arg.channel").asRequired()))
                .addSubCommand("list_type", "command.channel.sub.listType", argsBuilder()
                        .add(SimpleArgument.string("type", "command.channel.sub.listType.arg.type").withAutoComplete()))
                .addSubCommand("list", "command.channel.sub.list")
                .addSubCommand("announcement", "command.channel.sub.announcement", argsBuilder()
                        .add(SimpleArgument.bool("active", "command.channel.sub.announcement.arg.active"))
                        .add(SimpleArgument.channel("channel", "command.channel.sub.announcement.arg.where"))
                        .add(SimpleArgument.string("where", "command.channel.sub.announcement.arg.channel").withAutoComplete()))
                .withPermission());
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var subCmd = event.getSubcommandName();
        var channels = guilds.guild(event.getGuild()).settings().thanking().channels();
        if ("set".equalsIgnoreCase(subCmd)) {
            set(event, context, channels);
        }
        if ("add".equalsIgnoreCase(subCmd)) {
            add(event, context, channels);
        }
        if ("remove".equalsIgnoreCase(subCmd)) {
            remove(event, context, channels);
        }
        if ("list_type".equalsIgnoreCase(subCmd)) {
            whitelist(event, context, channels);
        }
        if ("addAll".equalsIgnoreCase(subCmd)) {
            addAll(event, context, channels);
        }
        if ("list".equalsIgnoreCase(subCmd)) {
            list(event, context, channels);
        }
        if ("announcement".equals(subCmd)) {
            announcements(event, context);
        }
    }

    private void announcements(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var announcements = guilds.guild(event.getGuild()).settings().announcements();
        if (event.getOption("active") != null) {
            if (announcements.active(event.getOption("active", OptionMapping::getAsBoolean))) {
                event.reply(context.localize("command.channel.sub.announcement.active.true")).queue();
            } else {
                event.reply(context.localize("command.channel.sub.announcement.active.false")).queue();
            }
            return;
        }
        if (event.getOption("where") != null) {
            if (announcements.sameChannel("same channel".equalsIgnoreCase(event.getOption("where", OptionMapping::getAsString)))) {
                event.reply(context.localize("command.channel.sub.announcement.sameChannel.true")).queue();
            } else {
                event.reply(context.localize("command.channel.sub.announcement.sameChannel.false")).queue();
            }
            return;
        }
        if (event.getOption("channel") != null) {
            var channel = event.getOption("channel");
            if (channel.getChannelType() != ChannelType.TEXT) {
                event.reply(context.localize("error.onlyTextChannel")).setEphemeral(true).queue();
                return;
            }

            announcements.channel(channel.getAsTextChannel());
            event.reply(context.localize("command.channel.sub.announcement.channel.set", Replacement.createMention(channel.getAsTextChannel()))).queue();
            return;
        }
        if (!announcements.isActive()) {
            event.reply(context.localize("command.channel.sub.announcement.active.false")).queue();
            return;
        }
        if (announcements.isSameChannel()) {
            event.reply(context.localize("command.channel.sub.announcement.sameChannel.true")).queue();
            return;
        }

        event.reply(context.localize("command.channel.sub.announcement.channel.set",
                Replacement.create("CHANNEL", MentionUtil.channel(announcements.channelId())))).queue();
    }

    private void whitelist(SlashCommandInteractionEvent event, SlashCommandContext context, Channels channels) {
        if (event.getOptions().isEmpty()) {
            event.reply(context.localize("command.channel.sub.whitelist." + channels.isWhitelist())).queue();
            return;
        }
        var whitelist = "whitelist".equalsIgnoreCase(event.getOption("type").getAsString());

        event.reply(context.localize("command.channel.sub.listType." + channels.listType(whitelist))).queue();
    }

    private void add(SlashCommandInteractionEvent event, SlashCommandContext context, Channels channels) {
        var channel = event.getOption("channel").getAsMessageChannel();
        if (channel == null || channel.getType() != ChannelType.TEXT) {
            event.reply(context.localize("error.invalidChannel")).setEphemeral(true).queue();
            return;
        }
        channels.add(channel);
        event.reply(
                context.localize("command.channel.sub.add.added",
                        Replacement.create("CHANNEL", channel.getAsMention()))).queue();
    }

    private void addAll(SlashCommandInteractionEvent event, SlashCommandContext context, Channels channels) {
        FilterUtil.getAccessableTextChannel(event.getGuild()).forEach(channels::add);
        event.reply(context.localize("command.channel.sub.addAll.added")).queue();
    }

    private void remove(SlashCommandInteractionEvent event, SlashCommandContext context, Channels channels) {
        var channel = event.getOption("channel").getAsMessageChannel();
        if (channel == null || channel.getType() != ChannelType.TEXT) {
            event.reply(context.localize("error.invalidChannel")).setEphemeral(true).queue();
            return;
        }
        channels.remove(channel);

        event.reply(context.localize("command.channel.sub.remove.removed",
                Replacement.create("CHANNEL", channel.getAsMention()))).queue();
    }

    private void list(SlashCommandInteractionEvent event, SlashCommandContext context, Channels channels) {
        event.reply(getChannelList(channels, context)).queue();
    }

    private String getChannelList(Channels channels, SlashCommandContext context) {
        var channelNames = channels.channels().stream().map(IMentionable::getAsMention).collect(Collectors.joining(", "));
        var message = "command.channel.sub.list." + (channels.isWhitelist() ? "whitelist" : "blacklist");
        return context.localize(message, Replacement.create("CHANNEL", channelNames));
    }

    private void set(SlashCommandInteractionEvent event, SlashCommandContext context, Channels channels) {
        var channel = event.getOption("channel").getAsMessageChannel();
        if (channel == null || channel.getType() != ChannelType.TEXT) {
            event.reply(context.localize("error.invalidChannel")).setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();
        channels.clear();
        channels.add(channel);
        event.getHook().editOriginal(context.localize("command.channel.sub.set.set",
                Replacement.create("CHANNEL", channel.getAsMention()))).queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, SlashCommandContext slashCommandContext) {
        if ("type".equals(event.getFocusedOption().getName())) {
            event.replyChoices(Completion.complete(event.getFocusedOption().getValue(), "whitelist", "blacklist")).queue();
        }
        if ("where".equals(event.getFocusedOption().getName())) {
            event.replyChoices(Completion.complete("", "same channel", "custom channel")).queue();
        }
    }
}
