package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.util.FilterUtil;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Channel extends SimpleCommand {
    private final GuildData guildData;

    public Channel(DataSource dataSource) {
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
                .withPermission());
        guildData = new GuildData(dataSource);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var subCmd = event.getSubcommandName();
        if ("set".equalsIgnoreCase(subCmd)) {
            set(event, context);
        }
        if ("add".equalsIgnoreCase(subCmd)) {
            add(event, context);
        }
        if ("remove".equalsIgnoreCase(subCmd)) {
            remove(event, context);
        }
        if ("list_type".equalsIgnoreCase(subCmd)) {
            whitelist(event, context);
        }
        if ("addAll".equalsIgnoreCase(subCmd)) {
            addAll(event, context);
        }
        if ("list".equalsIgnoreCase(subCmd)) {
            list(event, context);
        }
    }

    private void whitelist(SlashCommandInteractionEvent event, SlashCommandContext context) {
        if (event.getOptions().isEmpty()) {
            var guildSettings = guildData.getGuildSettings(event.getGuild());
            var channelWhitelist = guildSettings.thankSettings().isChannelWhitelist();
            event.reply(context.localize("command.channel.sub.whitelist." + channelWhitelist)).queue();
            return;
        }
        var whitelist = "whitelist".equalsIgnoreCase(event.getOption("type").getAsString());
        guildData.setChannelListType(event.getGuild(), whitelist);
        event.reply(context.localize("command.channel.sub.listType." + whitelist)).queue();
    }

    private void add(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var channel = event.getOption("channel").getAsMessageChannel();
        if (channel == null || channel.getType() != ChannelType.TEXT) {
            event.reply(context.localize("error.invalidChannel")).setEphemeral(true).queue();
            return;
        }

        guildData.addChannel(event.getGuild(), channel);
        event.reply(
                context.localize("command.channel.sub.add.added",
                        Replacement.create("CHANNEL", channel.getAsMention()))).queue();
    }

    private void addAll(SlashCommandInteractionEvent event, SlashCommandContext context) {
        FilterUtil.getAccessableTextChannel(event.getGuild()).forEach(c -> guildData.addChannel(event.getGuild(), c));
        event.reply(context.localize("command.channel.sub.addAll.added")).queue();
    }

    private void remove(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var channel = event.getOption("channel").getAsMessageChannel();
        if (channel == null || channel.getType() != ChannelType.TEXT) {
            event.reply(context.localize("error.invalidChannel")).setEphemeral(true).queue();
            return;
        }
        guildData.removeChannel(event.getGuild(), channel);

        event.reply(context.localize("command.channel.sub.remove.removed",
                Replacement.create("CHANNEL", channel.getAsMention()))).queue();
    }

    private void list(SlashCommandInteractionEvent event, SlashCommandContext context) {
        event.reply(getChannelList(guildData.getGuildSettings(event.getGuild()), context)).queue();
    }

    private String getChannelList(GuildSettings settings, SlashCommandContext context) {
        var channelNames = DiscordResolver
                .getValidTextChannelsById(
                        settings.guild(), new ArrayList<>(settings.thankSettings().activeChannel()))
                .stream().map(IMentionable::getAsMention).collect(Collectors.joining(", "));
        var message = "command.channel.sub.list." + (settings.thankSettings().isChannelWhitelist() ? "whitelist" : "blacklist");
        return context.localize(message, Replacement.create("CHANNEL", channelNames));
    }

    private void set(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var channel = event.getOption("channel").getAsMessageChannel();
        if (channel == null || channel.getType() != ChannelType.TEXT) {
            event.reply(context.localize("error.invalidChannel")).setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();
        guildData.clearChannel(event.getGuild());
        guildData.addChannel(event.getGuild(), channel);
        event.getHook().editOriginal(context.localize("command.channel.sub.set.set",
                Replacement.create("CHANNEL", channel.getAsMention()))).queue();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, SlashCommandContext slashCommandContext) {
        if ("type".equals(event.getFocusedOption().getName())) {
            event.replyChoices(Completion.complete(event.getFocusedOption().getValue(), "whitelist", "blacklist")).queue();
        }
    }
}
