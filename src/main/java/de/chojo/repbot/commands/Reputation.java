package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.text.TextFormatting;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.ReputationRole;
import de.chojo.repbot.util.TextGenerator;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.sql.DataSource;

public class Reputation extends SimpleCommand {
    private final ReputationData reputationData;
    private final GuildData guildData;
    private final Localizer loc;
    private static final int BAR_SIZE = 20;

    public Reputation(DataSource dataSource, Localizer localizer) {
        super("reputation",
                new String[] {"rep"},
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
            var reputation = reputationData.getReputation(eventWrapper.getGuild(), eventWrapper.getAuthor()).orElse(0);
            eventWrapper.replyNonMention(getUserRepEmbed(eventWrapper, eventWrapper.getMember(), reputation)).queue();
            return true;
        }

        var optSubComd = context.argString(0);
        if (optSubComd.isEmpty()) {
            return false;
        }
        var subCmd = optSubComd.get();
        if ("top".equalsIgnoreCase(subCmd)) {
            var page = context.argInt(1).orElse(1);
            var ranking = reputationData.getRanking(eventWrapper.getGuild(), 10, (page - 1) * 10);
            var builder = TextFormatting.getTableBuilder(ranking,
                    loc.localize("words.name", eventWrapper),
                    loc.localize("words.reputation", eventWrapper));
            for (var reputationUser : ranking) {
                var memberById = eventWrapper.getGuild().retrieveMemberById(reputationUser.getUserId()).complete();
                builder.setNextRow(memberById == null ? loc.localize("words.unknown", eventWrapper) : memberById.getEffectiveName(),
                        String.valueOf(reputationUser.getReputation()));
            }
            eventWrapper.replyNonMention(builder.toString()).queue();
            return true;
        }
        var guildMember = DiscordResolver.getGuildMember(eventWrapper.getGuild(), subCmd);
        if (guildMember.isEmpty()) {
            eventWrapper.replyErrorAndDelete(loc.localize("error.userNotFound", eventWrapper), 10);
            return true;
        }
        var reputation = reputationData.getReputation(eventWrapper.getGuild(), guildMember.get().getUser()).orElse(0);
        eventWrapper.replyNonMention(getUserRepEmbed(eventWrapper, guildMember.get(), reputation)).queue();
        return true;
    }

    private MessageEmbed getUserRepEmbed(MessageEventWrapper eventWrapper, Member member, long reputation) {
        var current = guildData.getCurrentReputationRole(member.getGuild(), reputation);
        var next = guildData.getNextReputationRole(member.getGuild(), reputation);

        var currentRoleRep = current.map(ReputationRole::getReputation).orElse(0L);
        var nextRoleRep = next.map(ReputationRole::getReputation).orElse(currentRoleRep);
        var progess = (double) (reputation - currentRoleRep) / (double) (nextRoleRep - currentRoleRep);

        var progressBar = TextGenerator.progressBar(progess, BAR_SIZE);

        var level = current.map(r -> r.getRole(member.getGuild())).map(IMentionable::getAsMention).orElse("none");

        var currProgress = String.valueOf(reputation - currentRoleRep);
        var nextLevel = nextRoleRep.equals(currentRoleRep) ? "\uA74E" : String.valueOf(nextRoleRep - currentRoleRep);
        return new LocalizedEmbedBuilder(loc, eventWrapper)
                .setTitle(loc.localize("command.reputation.profile.title", eventWrapper,
                        Replacement.create("NAME", member.getEffectiveName())))
                .addField("words.level", level, true)
                .addField(loc.localize("words.reputation", eventWrapper), Format.BOLD.apply(String.valueOf(reputation)), true)
                .addField("command.reputation.profile.nextLevel", currProgress + "/" + nextLevel + "  " + progressBar, false)
                .setThumbnail(member.getUser().getEffectiveAvatarUrl())
                .setColor(member.getColor())
                .build();
    }
}
