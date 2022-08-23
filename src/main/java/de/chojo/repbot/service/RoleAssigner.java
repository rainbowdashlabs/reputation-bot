package de.chojo.repbot.service;

import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.dao.provider.Guilds;
import de.chojo.repbot.dao.snapshots.ReputationRank;
import de.chojo.repbot.util.Roles;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.concurrent.Task;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.chojo.repbot.util.Guilds.prettyName;
import static org.slf4j.LoggerFactory.getLogger;

public class RoleAssigner {
    private static final Logger log = getLogger(RoleAssigner.class);
    private final Guilds guilds;
    private final ILocalizer localizer;

    public RoleAssigner(Guilds guilds, ILocalizer localizer) {
        this.guilds = guilds;
        this.localizer = localizer;
    }

    /**
     * Updates the user roles. Will handle excpetions and send a message if the role could not be assigned.
     *
     * @param member  member to update
     * @param channel channel to send the message to
     * @return the new highest role of the member, if it changed.
     */
    public Optional<ReputationRank> updateReporting(Member member, GuildMessageChannel channel) {
        try {
            return update(member);
        } catch (RoleAccessException e) {
            channel.sendMessage(localizer.localize("error.roleAccess", channel.getGuild(),
                            Replacement.createMention("ROLE", e.role())))
                    .allowedMentions(Collections.emptyList())
                    .queue();
        }
        return Optional.empty();
    }

    /**
     * Update the member role. Ignores any thrown {@link RoleAccessException}.
     *
     * @param member member to update
     * @return the new highest role of the member, if it changed.
     */
    public Optional<ReputationRank> updateSilent(Member member) {
        try {
            return update(member);
        } catch (RoleAccessException e) {
            //ignore
        }
        return Optional.empty();
    }

    /**
     * Updates the rank of the member.
     *
     * @param member member to update
     * @return the new highest role of the member, if it changed.
     * @throws RoleAccessException if the role cant be accessed
     */
    public Optional<ReputationRank> update(@Nullable Member member) throws RoleAccessException {
        if (member == null) return Optional.empty();
        log.debug("Updating {} on {}", member.getId(), prettyName(member.getGuild()));
        var guild = member.getGuild();
        var repGuild = guilds.guild(member.getGuild());
        var reputation = repGuild.reputation().user(member);
        var settings = repGuild.settings();

        var roles = settings.ranks().currentRanks(reputation)
                .stream()
                .map(r -> guild.getRoleById(r.roleId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var removed = cleanMemberRoles(member, roles);
        var added = addMemberRoles(member, roles);

        var changed = removed || added;

        if (!changed) {
            return Optional.empty();
        }

        return settings.ranks().currentRank(reputation);
    }

    private boolean cleanMemberRoles(Member member, Set<Role> roles) throws RoleAccessException {
        var guild = member.getGuild();

        var reputationRoles = guilds.guild(guild).settings().ranks().ranks()
                .stream()
                .map(ReputationRank::roleId)
                .map(guild::getRoleById)
                .filter(Objects::nonNull)
                .toList();
        var changed = false;

        for (var role : reputationRoles) {
            assertInteract(role, member.getGuild());
            if (roles.contains(role)) continue;
            if (member.getRoles().contains(role)) {
                log.debug("Removing role {} on {}", Roles.prettyName(role), prettyName(guild));
                guild.removeRoleFromMember(member, role).complete();
                changed = true;
            }
        }
        return changed;
    }

    private boolean addMemberRoles(Member member, Set<Role> roles) {
        var guild = member.getGuild();
        if (new HashSet<>(member.getRoles()).containsAll(roles)) return false;

        var changed = false;

        for (var role : roles) {
            assertInteract(role, guild);
            if (!member.getRoles().contains(role)) {
                log.debug("Assigning role {} on {}", Roles.prettyName(role), prettyName(guild));
                guild.addRoleToMember(member, role).complete();
                changed = true;
            }
        }
        return changed;
    }

    private void assertInteract(Role role, Guild guild) throws RoleAccessException {
        if (!guild.getSelfMember().canInteract(role)) {
            throw new RoleAccessException(role);
        }
    }

    public Task<Void> updateBatch(Guild guild) {
        log.info("Started batch update for guild {}", prettyName(guild));
        return guild.loadMembers(this::updateSilent);
    }
}
