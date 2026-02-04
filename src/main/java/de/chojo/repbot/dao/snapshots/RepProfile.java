/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots;

import de.chojo.jdautil.localization.LocalizationContext;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.MentionUtil;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.reputation.sub.RepUser;
import de.chojo.repbot.util.Text;
import de.chojo.sadu.mapper.wrapper.Row;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public static RepProfile buildProfile(RepUser repuser, Row rs) throws SQLException {
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

    @Deprecated(forRemoval = true)
    public static RepProfile buildReceivedRanking(Row rs) throws SQLException {
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

    @Deprecated(forRemoval = true)
    public static RepProfile buildGivenRanking(Row rs) throws SQLException {
        return new RepProfile(null,
                0,
                rs.getLong("rank_donated"),
                rs.getLong("user_id"),
                0,
                0,
                0,
                rs.getLong("donated")
        );
    }

    public String fancyString(int maxRank) {
        var length = String.valueOf(maxRank).length();
        var rank = StringUtils.rightPad(String.valueOf(this.rank), length);
        return "`" + rank + "` **|** " + MentionUtil.user(userId) + " ➜ " + reputation;
    }

    public MessageEmbed publicProfile(Configuration configuration, LocalizationContext localizer, boolean detailed) {
        return getBaseBuilder(configuration, localizer, detailed).build();
    }

    public MessageEmbed adminProfile(Configuration configuration, LocalizationContext localizer) {
        var build = getBaseBuilder(configuration, localizer, false);
        build.addField("words.rawReputation", String.valueOf(rawReputation()), true)
             .addField("words.reputationOffset", String.valueOf(repOffset()), true)
             .addField("words.donated", String.valueOf(donated()), true);
        return build.build();
    }

    public Optional<Member> resolveMember(Guild guild) {
        try {
            return Optional.ofNullable(guild.retrieveMemberById(userId()).complete());
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private EmbedBuilder getBaseBuilder(Configuration configuration, LocalizationContext localizer, boolean detailed) {
        var ranks = repUser.reputation().repGuild().settings().ranks();
        var current = ranks.currentRank(repUser);
        var next = ranks.nextRank(repUser);

        var currentRoleRep = current.map(ReputationRank::reputation).orElse(0);
        var nextRoleRep = next.map(ReputationRank::reputation).orElse(currentRoleRep);
        var progress = (double) (reputation() - currentRoleRep) / (nextRoleRep - currentRoleRep);

        var progressBar = Text.progressBar(progress, BAR_SIZE);

        var level = current.flatMap(r -> r.getRole(repUser.member().getGuild())).map(IMentionable::getAsMention)
                           .orElse("/");

        var currProgress = String.valueOf(reputation() - currentRoleRep);
        var nextLevel = nextRoleRep.equals(currentRoleRep) ? "Ꝏ" : String.valueOf(nextRoleRep - currentRoleRep);

        var build = new LocalizedEmbedBuilder(localizer);
        if (detailed) {
            build.setAuthor("element.profile.title", null, repUser.member().getEffectiveAvatarUrl(),
                    Replacement.create("NAME", repUser.member().getEffectiveName()));
            build.addField("words.rankreceived", rank() + "", true);
            build.addField("words.rankdonated", rankDonated() + "", true);
            build.addBlankField(false);
        } else {
            build.setAuthor("%s$%s$".formatted(rank() != 0 ? "#" + rank() + " " : "", "element.profile.title"),
                    null, repUser.member().getEffectiveAvatarUrl(),
                    Replacement.create("NAME", repUser.member().getEffectiveName()));
        }
        build.addField("words.level", level, true)
             .addField("words.reputation", Format.BOLD.apply(String.valueOf(reputation())), true)
             .addField("words.donated", Format.BOLD.apply(String.valueOf(donated())), true)
             .addField("element.profile.nextLevel", "```ANSI%n%s/%s  %s```".formatted(currProgress, nextLevel, progressBar), false)
             .setColor(repUser.member().getColor());
        var badge = configuration.badges().badge((int) rank());
        badge.ifPresent(build::setThumbnail);

        if (detailed) {
            addDetails(build);
        }

        return build;
    }

    private void addDetails(EmbedBuilder build) {
        var entries = 5;
        String topDonor = repUser.reputation().ranking().user().given().defaultRanking(entries, repUser.member()).page(0)
                                 .stream().map(RankingEntry::simpleString).collect(Collectors.joining("\n"));
        String topReceiver = repUser.reputation().ranking().user().received().defaultRanking(entries, repUser.member()).page(0)
                                    .stream().map(RankingEntry::simpleString).collect(Collectors.joining("\n"));
        var mostReceivedChannel = repUser.mostReceivedChannel(entries).stream().map(ChannelStats::fancyString).collect(Collectors.joining("\n"));
        var mostGivenChannel = repUser.mostGivenChannel(entries).stream().map(ChannelStats::fancyString).collect(Collectors.joining("\n"));

        // TODO: Great case for components v2

        build.addField("element.profile.topdonor", topDonor, true);
        build.addField("element.profile.topreceiver", topReceiver, true);
        build.addBlankField(false);
        build.addField("element.profile.mostgivenchannel", mostGivenChannel, true);
        build.addField("element.profile.mostreceivedchannel", mostReceivedChannel, true);
    }
}
