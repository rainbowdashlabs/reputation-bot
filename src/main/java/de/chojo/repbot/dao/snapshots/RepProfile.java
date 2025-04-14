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

/**
 * Snapshot of a user reputation profile.
 *
 * @param repUser the reputation user
 * @param rank the rank of the user
 * @param rankDonated the rank based on donations
 * @param userId the ID of the user
 * @param reputation the reputation points of the user
 * @param repOffset the reputation offset
 * @param rawReputation the raw reputation points
 * @param donated the amount donated
 */
public record RepProfile(RepUser repUser, long rank, long rankDonated, long userId, long reputation, long repOffset,
                         long rawReputation, long donated) {
    private static final int BAR_SIZE = 20;

    /**
     * Creates an empty RepProfile for the specified user.
     *
     * @param repuser the reputation user
     * @param user the user
     * @return an empty RepProfile
     */
    public static RepProfile empty(RepUser repuser, User user) {
        return new RepProfile(repuser, 0, user.getIdLong(), 0, 0, 0, 0, 0);
    }

    /**
     * Builds a RepProfile from the given database row.
     *
     * @param repuser the reputation user
     * @param rs the database row
     * @return a RepProfile
     * @throws SQLException if a database access error occurs
     */
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

    /**
     * Builds a RepProfile for received ranking from the given database row.
     *
     * @param rs the database row
     * @return a RepProfile
     * @throws SQLException if a database access error occurs
     */
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

    /**
     * Generates a fancy string representation of the profile.
     *
     * @param maxRank the maximum rank
     * @return a fancy string representation
     */
    public String fancyString(int maxRank) {
        var length = String.valueOf(maxRank).length();
        var rank = StringUtils.rightPad(String.valueOf(this.rank), length);
        return "`" + rank + "` **|** " + MentionUtil.user(userId) + " ➜ " + reputation;
    }

    /**
     * Generates a public profile message embed.
     *
     * @param configuration the configuration
     * @param localizer the localization context
     * @return a public profile message embed
     */
    public MessageEmbed publicProfile(Configuration configuration, LocalizationContext localizer) {
        return getBaseBuilder(configuration, localizer).build();
    }

    /**
     * Generates an admin profile message embed.
     *
     * @param configuration the configuration
     * @param localizer the localization context
     * @return an admin profile message embed
     */
    public MessageEmbed adminProfile(Configuration configuration, LocalizationContext localizer) {
        var build = getBaseBuilder(configuration, localizer);
        build.addField("words.rawReputation", String.valueOf(rawReputation()), true)
             .addField("words.reputationOffset", String.valueOf(repOffset()), true)
             .addField("words.donated", String.valueOf(donated()), true);
        return build.build();
    }

    /**
     * Creates a base embed builder for the profile.
     *
     * @param configuration the configuration
     * @param localizer the localization context
     * @return an embed builder
     */
    private EmbedBuilder getBaseBuilder(Configuration configuration, LocalizationContext localizer) {
        var ranks = repUser.reputation().repGuild().settings().ranks();
        var current = ranks.currentRank(repUser);
        var next = ranks.nextRank(repUser);

        var currentRoleRep = current.map(ReputationRank::reputation).orElse(0L);
        var nextRoleRep = next.map(ReputationRank::reputation).orElse(currentRoleRep);
        var progess = (double) (reputation() - currentRoleRep) / (nextRoleRep - currentRoleRep);

        var progressBar = Text.progressBar(progess, BAR_SIZE);

        var level = current.flatMap(r -> r.getRole(repUser.member().getGuild())).map(IMentionable::getAsMention)
                           .orElse("/");

        var currProgress = String.valueOf(reputation() - currentRoleRep);
        var nextLevel = nextRoleRep.equals(currentRoleRep) ? "Ꝏ" : String.valueOf(nextRoleRep - currentRoleRep);
        var build = new LocalizedEmbedBuilder(localizer)
                .setAuthor("%s$%s$".formatted(rank() != 0 ? "#" + rank() + " " : "", "element.profile.title"),
                        null, repUser.member().getEffectiveAvatarUrl(),
                        Replacement.create("NAME", repUser.member().getEffectiveName()))
                .addField("words.level", level, true)
                .addField("words.reputation", Format.BOLD.apply(String.valueOf(reputation())), true)
                .addField("element.profile.nextLevel", "```ANSI%n%s/%s  %s```".formatted(currProgress, nextLevel, progressBar), false)
                .setColor(repUser.member().getColor());
        var badge = configuration.badges().badge((int) rank());
        badge.ifPresent(build::setThumbnail);
        return build;
    }

    /**
     * Resolves the member associated with this profile in the given guild.
     *
     * @param guild the guild
     * @return an optional containing the member if found, otherwise empty
     */
    public Optional<Member> resolveMember(Guild guild) {
        try {
            return Optional.ofNullable(guild.retrieveMemberById(userId()).complete());
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
