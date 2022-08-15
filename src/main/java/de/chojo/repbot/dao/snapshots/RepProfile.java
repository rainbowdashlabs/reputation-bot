package de.chojo.repbot.dao.snapshots;

import de.chojo.jdautil.localization.LocalizationContext;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.reputation.sub.RepUser;
import de.chojo.repbot.util.TextGenerator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Snapshot of a user reputation profile.
 */
public record RepProfile(RepUser repUser, long rank, long rankDonated, long userId, long reputation, long repOffset,
                         long rawReputation,
                         long donated) {
    private static final int BAR_SIZE = 20;

    public static RepProfile empty(RepUser repuser, User user) {
        return new RepProfile(repuser, 0, user.getIdLong(), 0, 0, 0, 0, 0);
    }

    public static RepProfile buildProfile(RepUser repuser, ResultSet rs) throws SQLException {
        return new RepProfile(repuser,
                rs.getLong("rank"),
                rs.getLong("rank_donated"),
                rs.getLong("user_id"),
                rs.getLong("reputation"),
                rs.getLong("rep_offset"),
                rs.getLong("raw_reputation"),
                rs.getLong("donated")
        );
    }

    public static RepProfile buildReceivedRanking(ResultSet rs) throws SQLException {
        return new RepProfile(null,
                rs.getLong("rank"),
                0,
                rs.getLong("user_id"),
                rs.getLong("reputation"),
                0,
                0,
                0
        );
    }

    public String fancyString(int maxRank) {
        var length = String.valueOf(maxRank).length();
        var rank = StringUtils.rightPad(String.valueOf(this.rank), length);
        return "`" + rank + "` **|** " + MentionUtil.user(userId) + " ➜ " + reputation;
    }

    public MessageEmbed publicProfile(Configuration configuration, LocalizationContext localizer) {
        return getBaseBuilder(configuration, localizer).build();
    }

    public MessageEmbed adminProfile(Configuration configuration, LocalizationContext localizer) {
        var build = getBaseBuilder(configuration, localizer);
        build.addField("words.rawReputation", String.valueOf(rawReputation()), true)
                .addField("words.reputationOffset", String.valueOf(repOffset()), true)
                .addField("words.donated", String.valueOf(donated()), true);
        return build.build();
    }

    private EmbedBuilder getBaseBuilder(Configuration configuration, LocalizationContext localizer) {
        var ranks = repUser.reputation().repGuild().settings().ranks();
        var current = ranks.currentRank(repUser);
        var next = ranks.nextRank(repUser);

        var currentRoleRep = current.map(ReputationRank::reputation).orElse(0L);
        var nextRoleRep = next.map(ReputationRank::reputation).orElse(currentRoleRep);
        var progess = (double) (reputation() - currentRoleRep) / (double) (nextRoleRep - currentRoleRep);

        var progressBar = TextGenerator.progressBar(progess, BAR_SIZE);

        var level = current.map(r -> r.getRole(repUser.member().getGuild())).map(IMentionable::getAsMention).orElse("/");

        var currProgress = String.valueOf(reputation() - currentRoleRep);
        var nextLevel = nextRoleRep.equals(currentRoleRep) ? "Ꝏ" : String.valueOf(nextRoleRep - currentRoleRep);
        var build = new LocalizedEmbedBuilder(localizer)
                .setAuthor("%s%s".formatted(rank() != 0 ? "#" + rank() + " " : "", "element.profile.title"),
                        null, repUser.member().getEffectiveAvatarUrl(),
                        Replacement.create("NAME", repUser.member().getEffectiveName()))
                .addField("words.level", level, true)
                .addField("words.reputation", Format.BOLD.apply(String.valueOf(reputation())), true)
                .addField("element.profile.nextLevel", currProgress + "/" + nextLevel + "  " + progressBar, false)
                .setColor(repUser.member().getColor());
        var badge = configuration.badges().badge((int) rank());
        badge.ifPresent(build::setThumbnail);
        return build;
    }
}
