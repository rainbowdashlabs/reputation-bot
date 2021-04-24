package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.text.TextFormatting;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.ReputationRole;
import de.chojo.repbot.util.TextGenerator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.sql.DataSource;

public class Reputation extends SimpleCommand {
    private final ReputationData reputationData;
    private final GuildData guildData;
    private static final int BAR_SIZE = 20;

    public Reputation(DataSource dataSource, Localizer localizer) {
        super("reputation",
                new String[] {"rep"},
                "Information about your or others reputation.",
                "[user]",
                subCommandBuilder()
                        .add("top", "[page]", "Get the top list or a page on the list")
                        .build(),
                Permission.UNKNOWN);
        reputationData = new ReputationData(dataSource);
        guildData = new GuildData(dataSource);
    }

    @Override
    public boolean onCommand(MessageEventWrapper event, CommandContext context) {
        if (context.argsEmpty()) {
            var reputation = reputationData.getReputation(event.getGuild(), event.getAuthor()).orElse(0);
            event.replyNonMention(getUserRepEmbed(event.getMember(), reputation)).queue();
            return true;
        }

        var optSubComd = context.argString(0);
        if (optSubComd.isPresent()) {
            var subCmd = optSubComd.get();
            if ("top".equalsIgnoreCase(subCmd)) {
                var page = context.argInt(1).orElse(1);
                var ranking = reputationData.getRanking(event.getGuild(), 10, (page - 1) * 10);
                var builder = TextFormatting.getTableBuilder(ranking, "Name", "Reputation");
                for (var reputationUser : ranking) {
                    var memberById = event.getGuild().retrieveMemberById(reputationUser.getUserId()).complete();
                    builder.setNextRow(memberById == null ? "Unknown" : memberById.getEffectiveName(), String.valueOf(reputationUser.getReputation()));
                }
                event.replyNonMention(builder.toString()).queue();
                return true;
            }
            var guildMember = DiscordResolver.getGuildMember(event.getGuild(), subCmd);
            if (guildMember.isEmpty()) {
                event.replyErrorAndDelete("Could not find this user.", 10);
                return true;
            }
            var reputation = reputationData.getReputation(event.getGuild(), guildMember.get().getUser()).orElse(0);
            event.replyNonMention(getUserRepEmbed(guildMember.get(), reputation)).queue();
            return true;
        }
        return false;
    }

    private MessageEmbed getUserRepEmbed(Member member, long reputation) {
        var current = guildData.getCurrentReputationRole(member.getGuild(), reputation);
        var next = guildData.getNextReputationRole(member.getGuild(), reputation);

        var currentRoleRep = current.map(ReputationRole::getReputation).orElse(0L);
        var nextRoleRep = next.map(ReputationRole::getReputation).orElse(currentRoleRep);
        var progess = (double) (reputation - currentRoleRep) / (double) (nextRoleRep - currentRoleRep);

        var progressBar = TextGenerator.progressBar(progess, BAR_SIZE);

        var level = current.map(r -> r.getRole(member.getGuild())).map(IMentionable::getAsMention).orElse("none");

        var currProgress = String.valueOf(reputation - currentRoleRep);
        var nextLevel = nextRoleRep.equals(currentRoleRep) ? "\uA74E" : String.valueOf(nextRoleRep - currentRoleRep);
        return new EmbedBuilder()
                .setTitle("Reputation of: " + member.getEffectiveName())
                .addField("Level", level, true)
                .addField("Reputation " + reputation, "", true)
                .addField("Next Level", currProgress + "/" + nextLevel + "  " + progressBar, false)
                .setThumbnail(member.getUser().getEffectiveAvatarUrl())
                .setColor(member.getColor())
                .build();
    }
}
