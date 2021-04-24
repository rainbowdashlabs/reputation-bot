package de.chojo.repbot.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.parsing.DiscordResolver;
import de.chojo.jdautil.text.TextFormatting;
import de.chojo.jdautil.wrapper.CommandContext;
import de.chojo.jdautil.wrapper.MessageEventWrapper;
import de.chojo.repbot.data.ReputationData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.sql.DataSource;

public class Reputation extends SimpleCommand {

    private final ReputationData reputationData;

    public Reputation(DataSource dataSource) {
        super("reputation", new String[] {"rep"}, "Information about reputation", "rep [ [user] | top [page]]", Permission.UNKNOWN);
        reputationData = new ReputationData(dataSource);
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
                    var memberById = event.getGuild().getMemberById(reputationUser.getUserId());
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

    private MessageEmbed getUserRepEmbed(Member user, long reputation) {
        return new EmbedBuilder()
                .setTitle("Reputation of: " + user.getEffectiveName())
                .addField("Level", "0", false)
                .addField("Reputation", String.valueOf(reputation), false)
                .setThumbnail(user.getUser().getEffectiveAvatarUrl())
                .setColor(user.getColor())
                .build();
    }
}
