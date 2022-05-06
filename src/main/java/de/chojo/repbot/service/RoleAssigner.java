package de.chojo.repbot.service;

import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.snapshots.ReputationRank;
import de.chojo.repbot.dao.snapshots.RepProfile;
import de.chojo.repbot.util.Roles;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.concurrent.Task;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static de.chojo.repbot.util.Guilds.prettyName;
import static org.slf4j.LoggerFactory.getLogger;

public class RoleAssigner {
    private static final Logger log = getLogger(RoleAssigner.class);
    private final de.chojo.repbot.dao.provider.Guilds guilds;

    public RoleAssigner(de.chojo.repbot.dao.provider.Guilds guilds) {
        this.guilds = guilds;
    }

    public void update(@Nullable Member member) throws RoleAccessException {
        if (member == null) return;
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

        cleanMemberRoles(member, roles);

        if (new HashSet<>(member.getRoles()).containsAll(roles)) return;

        for (var role : roles) {
            assertInteract(role, member.getGuild());
            if (!member.getRoles().contains(role)) {
                log.debug("Assigning role {} on {}", Roles.prettyName(role), prettyName(guild));
                guild.addRoleToMember(member, role).complete();
            }
        }
    }

    private void cleanMemberRoles(Member member, Set<Role> roles) throws RoleAccessException {
        var guild = member.getGuild();

        var reputationRoles = guilds.guild(member.getGuild()).settings().ranks().ranks()
                .stream()
                .map(ReputationRank::roleId)
                .map(guild::getRoleById)
                .filter(Objects::nonNull).toList();
        for (var role : reputationRoles) {
            assertInteract(role, member.getGuild());
            if (roles.contains(role)) continue;
            if (member.getRoles().contains(role)) {
                log.debug("Removing role {} on {}", Roles.prettyName(role), prettyName(guild));
                guild.removeRoleFromMember(member, role).complete();
            }
        }
    }

    private void assertInteract(Role role, Guild guild) throws RoleAccessException {
        if (!guild.getSelfMember().canInteract(role)) {
            throw new RoleAccessException(role);
        }
    }

    public Task<Void> updateBatch(Guild guild) {
        log.info("Started batch update for guild {}", prettyName(guild));
        return guild.loadMembers(this::update);
    }
}
