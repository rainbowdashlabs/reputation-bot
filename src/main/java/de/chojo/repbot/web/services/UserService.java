/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.services;

import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.dao.snapshots.ChannelStats;
import de.chojo.repbot.web.cache.MemberCache;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.pojo.guild.MemberPOJO;
import de.chojo.repbot.web.pojo.ranking.RankingEntryStatPOJO;
import de.chojo.repbot.web.pojo.session.GuildSessionData;
import de.chojo.repbot.web.services.userservice.AdminProfile;
import de.chojo.repbot.web.services.userservice.DetailedProfile;
import de.chojo.repbot.web.services.userservice.UserProfile;

import java.util.List;

/**
 * Service providing profile data for a user within a guild context.
 */
public class UserService {
    private final MemberCache memberCache;

    public UserService(MemberCache memberCache) {
        this.memberCache = memberCache;
    }

    /**
     * Builds a {@link UserProfile} for the given user in the guild context.
     * Includes a {@link DetailedProfile} if the guild has the detailedProfile feature unlocked.
     * Includes an {@link AdminProfile} if the requesting user has the guild admin role.
     *
     * @param session          the guild session of the requesting user
     * @param targetUserId     the user ID of the profile to retrieve
     * @param guildSessionData the session data of the requesting user, used to determine admin access
     * @return the user profile, potentially with detailed and/or admin data
     */
    public UserProfile getProfile(GuildSession session, long targetUserId, GuildSessionData guildSessionData) {
        var repUser = session.repGuild().reputation().user(session.guild().getMemberById(targetUserId));
        var profile = repUser.profile();

        boolean isAdmin = guildSessionData != null && guildSessionData.accessLevel() == Role.GUILD_ADMIN;
        boolean detailedUnlocked =
                session.premiumValidator().features().detailedProfile().unlocked();

        DetailedProfile detailedProfile = null;
        if (detailedUnlocked) {
            var entries = 5;
            var topDonors =
                    repUser
                            .reputation()
                            .ranking()
                            .user()
                            .given()
                            .defaultRanking(entries, repUser.member())
                            .page(0)
                            .stream()
                            .map(e -> RankingEntryStatPOJO.generate(e, memberCache.get(session.guild(), e.userId())))
                            .toList();
            var topReceivers =
                    repUser
                            .reputation()
                            .ranking()
                            .user()
                            .received()
                            .defaultRanking(entries, repUser.member())
                            .page(0)
                            .stream()
                            .map(e -> RankingEntryStatPOJO.generate(e, memberCache.get(session.guild(), e.userId())))
                            .toList();
            List<ChannelStats> mostGivenChannels = repUser.mostGivenChannel(entries);
            List<ChannelStats> mostReceivedChannels = repUser.mostReceivedChannel(entries);
            detailedProfile = new DetailedProfile(topDonors, topReceivers, mostGivenChannels, mostReceivedChannels);
        }

        AdminProfile adminProfile = null;
        if (isAdmin) {
            adminProfile = new AdminProfile(profile.rawReputation(), profile.repOffset(), profile.donated());
        }

        var ranks = session.repGuild().settings().ranks();
        var current = ranks.currentRank(repUser);
        var next = ranks.nextRank(repUser);
        var currentRoleRep = current.map(r -> (long) r.reputation()).orElse(0L);
        var nextRoleRep = next.map(r -> (long) r.reputation()).orElse(currentRoleRep);
        long currentProgress = profile.reputation() - currentRoleRep;
        Long nextLevelReputation = nextRoleRep.equals(currentRoleRep) ? null : nextRoleRep;
        Long levelRoleId = current.flatMap(r -> r.getRole(session.guild()))
                .map(net.dv8tion.jda.api.entities.Role::getIdLong)
                .orElse(null);

        MemberPOJO memberPOJO = memberCache.get(session.guild(), targetUserId);

        return new UserProfile(
                memberPOJO,
                profile.rank(),
                profile.rankDonated(),
                profile.reputation(),
                profile.donated(),
                levelRoleId,
                currentProgress,
                nextLevelReputation,
                detailedProfile,
                adminProfile);
    }
}
