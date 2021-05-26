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
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.sql.DataSource;
import java.awt.Color;
import java.util.stream.Collectors;

public class Reputation extends SimpleCommand {
    private static final int BAR_SIZE = 20;
    private static final int TOP_PAGE_SIZE = 10;
    private final ReputationData reputationData;
    private final GuildData guildData;
    private final Localizer loc;

    public Reputation(DataSource dataSource, Localizer localizer) {
        super("reputation",
                new String[]{"rep"},
                "command.reputation.description",
                "[user]",
                subCommandBuilder()
                        .add("top", "[page]", "command.reputation.sub.top")
                        .build(),
                Permission.UNKNOWN);
        reputationData = new ReputationData(dataSource);
        guildData = new GuildData(dataSource);
        loc = localizer;
    }

    @Override
    public boolean onCommand(MessageEventWrapper eventWrapper, CommandContext context) {
        if (context.argsEmpty()) {
            var reputation = reputationData.getReputation(eventWrapper.getGuild(), eventWrapper.getAuthor()).orElse(ReputationUser.empty(eventWrapper.getAuthor()));
            eventWrapper.reply(getUserRepEmbed(eventWrapper, eventWrapper.getMember(), reputation)).queue();
            return true;
        }

        var optSubComd = context.argString(0);
        if (optSubComd.isEmpty()) {
            return false;
        }
        var subCmd = optSubComd.get();
        if ("top".equalsIgnoreCase(subCmd)) {
            return top(eventWrapper, context.subContext(subCmd));
        }
        var guildMember = DiscordResolver.getGuildMember(eventWrapper.getGuild(), subCmd);
        if (guildMember.isEmpty()) {
            eventWrapper.replyErrorAndDelete(eventWrapper.localize("error.userNotFound"), 10);
            return true;
        }
        var reputation = reputationData.getReputation(eventWrapper.getGuild(), guildMember.get().getUser()).orElse(ReputationUser.empty(eventWrapper.getAuthor()));
        eventWrapper.reply(getUserRepEmbed(eventWrapper, guildMember.get(), reputation)).queue();
        return true;
    }

    private boolean top(MessageEventWrapper eventWrapper, CommandContext context) {
        var page = context.argInt(0).orElse(1);
        var offset = (page - 1) * TOP_PAGE_SIZE;
        var ranking = reputationData.getRanking(eventWrapper.getGuild(), TOP_PAGE_SIZE, offset);

        var maxRank = offset + TOP_PAGE_SIZE;
        var rankString = ranking.stream().map(rank -> rank.fancyString(maxRank)).collect(Collectors.joining("\n"));

        var embed = new LocalizedEmbedBuilder(loc, eventWrapper)
                .setTitle(eventWrapper.localize("command.reputation.sub.top.ranking",
                        Replacement.create("GUILD", eventWrapper.getGuild().getName())))
                .setDescription(rankString)
                .setColor(Color.CYAN)
                .build();
        eventWrapper.reply(embed).queue();
        return true;
    }

    private MessageEmbed getUserRepEmbed(MessageEventWrapper eventWrapper, Member member, ReputationUser reputation) {
        var current = guildData.getCurrentReputationRole(member.getGuild(), reputation.getReputation());
        var next = guildData.getNextReputationRole(member.getGuild(), reputation.getReputation());

        var currentRoleRep = current.map(ReputationRole::getReputation).orElse(0L);
        var nextRoleRep = next.map(ReputationRole::getReputation).orElse(currentRoleRep);
        var progess = (double) (reputation.getReputation() - currentRoleRep) / (double) (nextRoleRep - currentRoleRep);

        var progressBar = TextGenerator.progressBar(progess, BAR_SIZE);

        var level = current.map(r -> r.getRole(member.getGuild())).map(IMentionable::getAsMention).orElse("none");

        var currProgress = String.valueOf(reputation.getReputation() - currentRoleRep);
        var nextLevel = nextRoleRep.equals(currentRoleRep) ? "\uA74E" : String.valueOf(nextRoleRep - currentRoleRep);
        return new LocalizedEmbedBuilder(loc, eventWrapper)
                .setTitle(
                        (reputation.getRank() != 0 ? "#" + reputation.getRank() + " " : "")
                                + eventWrapper.localize("command.reputation.profile.title",
                                Replacement.create("NAME", member.getEffectiveName())))
                .addField("words.level", level, true)
                .addField(eventWrapper.localize("words.reputation"), Format.BOLD.apply(String.valueOf(reputation.getReputation())), true)
                .addField("command.reputation.profile.nextLevel", currProgress + "/" + nextLevel + "  " + progressBar, false)
                .setThumbnail(member.getUser().getEffectiveAvatarUrl())
                .setColor(member.getColor())
                .build();
    }
}
