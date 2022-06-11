package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.dao.access.guild.settings.sub.thanking.Channels;
import de.chojo.repbot.dao.provider.Guilds;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.stream.Collectors;

public class Channel extends SimpleCommand {
    private final Guilds guilds;

    public Channel(Guilds guilds) {
        super(CommandMeta.builder("channel", "command.channel.description")
                .addSubCommand("set", "command.channel.sub.set", argsBuilder()
                        .add(SimpleArgument.channel("channel", "command.channel.sub.set.arg.channel").asRequired()))
                .addSubCommand("add", "command.channel.sub.add", argsBuilder()
                        .add(SimpleArgument.channel("channel", "command.channel.sub.add.arg.channel").asRequired()))
                .addSubCommand("remove", "command.channel.sub.remove", argsBuilder()
                        .add(SimpleArgument.channel("channel", "command.channel.sub.remove.arg.channel").asRequired()))
                .addSubCommand("list_type", "command.channel.sub.listType", argsBuilder()
                        .add(SimpleArgument.string("type", "command.channel.sub.listType.arg.type").withAutoComplete()))
                .addSubCommand("list", "command.channel.sub.list")
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
        if ("list".equalsIgnoreCase(subCmd)) {
            list(event, context, channels);
        }
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
        var channelType = event.getOption("channel").getChannelType();
        if (channelType != ChannelType.TEXT && channelType != ChannelType.CATEGORY) {
            event.reply(context.localize("error.onlyTextOrCategory")).setEphemeral(true).queue();
            return;
        }
        if (channelType == ChannelType.TEXT) {
            var channel = event.getOption("channel").getAsTextChannel();
            channels.add(channel);
            event.reply(
                    context.localize("command.channel.sub.add.added",
                            Replacement.create("CHANNEL", channel.getAsMention()))).queue();
        } else {
            var channel = event.getOption("channel").getAsGuildChannel();
            channels.add((Category) channel);
            event.reply(
                    context.localize("command.channel.sub.add.added",
                            Replacement.create("CHANNEL", channel.getAsMention()))).queue();
        }
    }

    private void remove(SlashCommandInteractionEvent event, SlashCommandContext context, Channels channels) {
        var channelType = event.getOption("channel").getChannelType();
        if (channelType != ChannelType.TEXT && channelType != ChannelType.CATEGORY) {
            event.reply(context.localize("error.onlyTextOrCategory")).setEphemeral(true).queue();
            return;
        }
        if (channelType == ChannelType.TEXT) {
            var channel = event.getOption("channel").getAsTextChannel();
            channels.remove(channel);
            event.reply(
                    context.localize("command.channel.sub.remove.removed",
                            Replacement.create("CHANNEL", channel.getAsMention()))).queue();
        } else {
            var channel = event.getOption("channel").getAsGuildChannel();
            channels.remove((Category) channel);
            event.reply(
                    context.localize("command.channel.sub.remove.removed",
                            Replacement.create("CHANNEL", channel.getAsMention()))).queue();
        }
    }

    private void list(SlashCommandInteractionEvent event, SlashCommandContext context, Channels channels) {
        event.replyEmbeds(getChannelList(channels, context)).queue();
    }

    private MessageEmbed getChannelList(Channels channels, SlashCommandContext context) {

        var channelNames = channels.channels().stream().map(IMentionable::getAsMention).limit(40).collect(Collectors.joining(", "));
        if (channels.channels().size() > 40) {
            channelNames += String.format("$%s$", "command.channel.list.more");
        }

        var categoryNames = channels.categories().stream().map(IMentionable::getAsMention).limit(40).collect(Collectors.joining(", "));
        if (channels.categories().size() > 40) {
            categoryNames += String.format("$%s$", "command.channel.list.more");
        }

        return new LocalizedEmbedBuilder(context.localizer())
                .setTitle(channels.isWhitelist() ? "command.channel.sub.list.whitelist" : "command.channel.sub.list.blacklist")
                .addField("words.channels", channelNames, false, Replacement.create("MORE", channels.channels().size() - 40))
                .addField("words.categories", categoryNames, false, Replacement.create("MORE", channels.channels().size() - 40))
                .build();
    }

    private void set(SlashCommandInteractionEvent event, SlashCommandContext context, Channels channels) {
        var channelType = event.getOption("channel").getChannelType();
        if (channelType != ChannelType.TEXT && channelType != ChannelType.CATEGORY) {
            event.reply(context.localize("error.onlyTextOrCategory")).setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();
        if (channelType == ChannelType.TEXT) {
            var channel = event.getOption("channel").getAsTextChannel();
            channels.clearChannel();
            channels.add(channel);
            event.getHook().editOriginal(
                    context.localize("command.channel.sub.set.set",
                            Replacement.create("CHANNEL", channel.getAsMention()))).queue();
        } else {
            var channel = event.getOption("channel").getAsGuildChannel();
            channels.clearCategories();
            channels.add((Category) channel);
            event.getHook().editOriginal(
                    context.localize("command.channel.sub.set.set",
                            Replacement.create("CHANNEL", channel.getAsMention()))).queue();
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, SlashCommandContext slashCommandContext) {
        if ("type".equals(event.getFocusedOption().getName())) {
            event.replyChoices(Completion.complete(event.getFocusedOption().getValue(), "whitelist", "blacklist")).queue();
        }
    }
}
