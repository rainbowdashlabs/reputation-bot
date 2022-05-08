package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.util.Colors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.stream.Collectors;

public class Dashboard extends SimpleCommand {
    private final Guilds guilds;

    public Dashboard(Guilds guilds) {
        super(CommandMeta.builder("dashboard", "command.dashboard.description"));
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        event.replyEmbeds(getDashboard(event.getGuild(), context)).queue();
    }

    private MessageEmbed getDashboard(Guild guild, SlashCommandContext context) {
        var reputation = guilds.guild(guild).reputation();
        var stats = reputation.stats();
        var top = reputation.ranking().total(5).page(0).stream()
                .map(r -> r.fancyString(5))
                .collect(Collectors.joining("\n"));

        return new LocalizedEmbedBuilder(context.localizer())
                .setTitle("command.dashboard.title",
                        Replacement.create("GUILD", guild.getName()))
                .setThumbnail(guild.getIconUrl() == null ? guild.getSelfMember().getUser().getAvatarUrl() : guild.getIconUrl())
                .setColor(Colors.Pastel.BLUE)
                .addField("command.dashboard.topUser", top, false)
                .addField("command.dashboard.totalReputation", String.valueOf(stats.totalReputation()), true)
                .addField("command.dashboard.weekReputation", String.valueOf(stats.weekReputation()), true)
                .addField("command.dashboard.todayReputation", String.valueOf(stats.todayReputation()), true)
                .addField("command.dashboard.topChannel", MentionUtil.channel(stats.topChannelId()), true)
                .build();
    }
}
