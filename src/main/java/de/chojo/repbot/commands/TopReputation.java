package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.ReputationRole;
import de.chojo.repbot.data.wrapper.ReputationUser;
import de.chojo.repbot.util.TextGenerator;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.sql.DataSource;
import java.awt.Color;
import java.util.stream.Collectors;

public class TopReputation extends SimpleCommand {
    private static final int BAR_SIZE = 20;
    private static final int TOP_PAGE_SIZE = 10;
    private final ReputationData reputationData;
    private final Localizer loc;

    public TopReputation(DataSource dataSource, Localizer localizer) {
        super("top",
                new String[]{"reptop"},
                "command.reputation.description",
                argsBuilder()
                        .add(OptionType.INTEGER, "page", "page")
                        .build(),
                Permission.UNKNOWN);
        reputationData = new ReputationData(dataSource);
        loc = localizer;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        var page = context.argInt(0).orElse(1);
        eventWrapper.reply(top(eventWrapper.getGuild(), page)).queue();
        return true;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        var page = event.getOption("page");
        var l = page == null ? 1 : page.getAsLong();
        event.reply(wrap(top(event.getGuild(), (int) l))).queue();
    }

    private MessageEmbed top(Guild guild, int page) {
        var offset = (page - 1) * TOP_PAGE_SIZE;
        var ranking = reputationData.getRanking(guild, TOP_PAGE_SIZE, offset);

        var maxRank = offset + TOP_PAGE_SIZE;
        var rankString = ranking.stream().map(rank -> rank.fancyString(maxRank)).collect(Collectors.joining("\n"));

        return new LocalizedEmbedBuilder(loc, guild)
                .setTitle(loc.localize("command.reputation.sub.top.ranking", guild,
                        Replacement.create("GUILD", guild.getName())))
                .setDescription(rankString)
                .setColor(Color.CYAN)
                .build();
    }
}
