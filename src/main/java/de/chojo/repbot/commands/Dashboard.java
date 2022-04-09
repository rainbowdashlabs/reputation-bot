package de.chojo.repbot.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.util.Colors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.sql.DataSource;
import java.util.stream.Collectors;

public class Dashboard extends SimpleCommand {
    private final ReputationData reputationData;

    public Dashboard(DataSource dataSource) {
        super(CommandMeta.builder("dashboard", "command.dashboard.description"));
        reputationData = new ReputationData(dataSource);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        event.replyEmbeds(getDashboard(event.getGuild(), context)).queue();
    }

    private MessageEmbed getDashboard(Guild guild, SlashCommandContext context) {
        var optStats = reputationData.getGuildReputationStats(guild);
        if (optStats.isEmpty()) return new EmbedBuilder().setTitle("None").build();
        var stats = optStats.get();
        var top3 = reputationData.getRanking(guild, 5, 1).stream()
                .map(r -> r.fancyString(5))
                .collect(Collectors.joining("\n"));

        return new LocalizedEmbedBuilder(context.localizer())
                .setTitle("command.dashboard.title",
                        Replacement.create("GUILD", guild.getName()))
                .setThumbnail(guild.getIconUrl() == null ? guild.getSelfMember().getUser().getAvatarUrl() : guild.getIconUrl())
                .setColor(Colors.Pastel.BLUE)
                .addField("command.dashboard.topUser", top3, false)
                .addField("command.dashboard.totalReputation", String.valueOf(stats.totalReputation()), true)
                .addField("command.dashboard.weekReputation", String.valueOf(stats.weekReputation()), true)
                .addField("command.dashboard.todayReputation", String.valueOf(stats.todayReputation()), true)
                .addField("command.dashboard.topChannel", MentionUtil.channel(stats.topChannelId()), true)
                .build();
    }
}
