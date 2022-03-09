package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.wrapper.GuildSettings;
import de.chojo.repbot.util.FilterUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Channel extends SimpleCommand {
    private final GuildData guildData;
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
                        .add("addall", "command.channel.sub.addAll")
                        .add("remove", "command.channel.sub.remove", argsBuilder()
                                .add(OptionType.CHANNEL, "channel", "channel", true)
                                .build()
                        )
                        .add("whitelist", "Use channel list as whitelist", argsBuilder()
                                .add(OptionType.BOOLEAN, "whitelist", "true to use list as whitelist", false)
                                .build())
                        .add("list", "command.channel.sub.list")
                        .build(),
                Permission.MANAGE_SERVER);
        guildData = new GuildData(dataSource);
        this.loc = loc;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var subCmd = event.getSubcommandName();
        if ("set".equalsIgnoreCase(subCmd)) {
            set(event);
        }
        if ("add".equalsIgnoreCase(subCmd)) {
            add(event);
        }
        if ("remove".equalsIgnoreCase(subCmd)) {
            remove(event);
        }
        if ("whitelist".equalsIgnoreCase(subCmd)) {
            whitelist(event);
        }
        if ("addAll".equalsIgnoreCase(subCmd)) {
            addAll(event);
        }
        if ("list".equalsIgnoreCase(subCmd)) {
            list(event);
        }
    }

    private void whitelist(SlashCommandInteractionEvent event) {
        if (event.getOptions().isEmpty()) {
            var guildSettings = guildData.getGuildSettings(event.getGuild());
            var channelWhitelist = guildSettings.thankSettings().isChannelWhitelist();
            event.reply(loc.localize("command.channel.sub.whitelist." + channelWhitelist, event.getGuild())).queue();
            return;
        }
        var whitelist = event.getOption("whitelist").getAsBoolean();
        guildData.setChannelListType(event.getGuild(), whitelist);
        event.reply(loc.localize("command.channel.sub.whitelist." + whitelist, event.getGuild())).queue();
    }

    private void add(SlashCommandInteractionEvent event) {
        var channel = event.getOption("channel").getAsMessageChannel();
        if (channel == null || channel.getType() != ChannelType.TEXT) {
            event.reply(loc.localize("error.invalidChannel")).setEphemeral(true).queue();
            return;
        }

        guildData.addChannel(event.getGuild(), channel);
        event.reply(
                loc.localize("command.channel.sub.add.added", event.getGuild(),
                        Replacement.create("CHANNEL", ((TextChannel) channel).getAsMention()))).queue();
    }

    private void addAll(SlashCommandInteractionEvent event) {
        FilterUtil.getAccessableTextChannel(event.getGuild()).forEach(c -> guildData.addChannel(event.getGuild(), c));
        event.reply(loc.localize("command.channel.sub.addAll.added", event.getGuild())).queue();
    }

    private void remove(SlashCommandInteractionEvent event) {
        var channel = event.getOption("channel").getAsMessageChannel();
        if (channel == null || channel.getType() != ChannelType.TEXT) {
            event.reply(loc.localize("error.invalidChannel")).setEphemeral(true).queue();
            return;
        }
        guildData.removeChannel(event.getGuild(), channel);

        event.reply(loc.localize("command.channel.sub.remove.removed",
                Replacement.create("CHANNEL", ((TextChannel) channel).getAsMention()))).queue();
    }

    private void list(SlashCommandInteractionEvent event) {
        event.reply(getChannelList(guildData.getGuildSettings(event.getGuild()))).queue();
    }

    private String getChannelList(GuildSettings settings) {
        var channelNames = DiscordResolver
                .getValidTextChannelsById(
                        settings.guild(), new ArrayList<>(settings.thankSettings().activeChannel()))
                .stream().map(IMentionable::getAsMention).collect(Collectors.joining(", "));
        var message = "command.channel.sub.list." + (settings.thankSettings().isChannelWhitelist() ? "whitelist" : "blacklist");
        return loc.localize(message, settings.guild(), Replacement.create("CHANNEL", channelNames));
    }

    private void set(SlashCommandInteractionEvent event) {
        var channel = event.getOption("channel").getAsMessageChannel();
        if (channel == null || channel.getType() != ChannelType.TEXT) {
            event.reply(loc.localize("error.invalidChannel")).setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();
        guildData.clearChannel(event.getGuild());
        guildData.addChannel(event.getGuild(), channel);
        event.getHook().editOriginal(loc.localize("command.channel.sub.set.set",
                Replacement.create("CHANNEL", ((TextChannel) channel).getAsMention()))).queue();
    }
}
