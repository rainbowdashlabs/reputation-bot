package de.chojo.repbot.service;

import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.ReputationRole;
import de.chojo.repbot.data.wrapper.ReputationUser;
import de.chojo.repbot.util.Guilds;
import de.chojo.repbot.util.Roles;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.utils.concurrent.Task;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class RoleAssigner {
    private static final Logger log = getLogger(RoleAssigner.class);
    private final GuildData guildData;
    private final ReputationData reputationData;

    public RoleAssigner(DataSource dataSource) {
        guildData = new GuildData(dataSource);
        reputationData = new ReputationData(dataSource);
    }

    public void update(@Nullable Member member) throws RoleAccessException {
        if (member == null) return;
        log.debug("Updating {} on {}", member.getId(), Guilds.prettyName(member.getGuild()));
        var guild = member.getGuild();
        var reputation = reputationData.getReputation(guild, member.getUser()).orElse(ReputationUser.empty(member.getUser()));
        var settings = guildData.getGuildSettings(member.getGuild());

        var roles = guildData.getCurrentReputationRole(guild, reputation.reputation(), settings.generalSettings().isStackRoles())
                .stream()
                .map(r -> guild.getRoleById(r.roleId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        cleanMemberRoles(member, roles);

        if (new HashSet<>(member.getRoles()).containsAll(roles)) return;

        for (var role : roles) {
            assertInteract(role, member.getGuild());
            if (!member.getRoles().contains(role)) {
                log.debug("Assigning role {} on {}", Roles.prettyName(role), Guilds.prettyName(guild));
                guild.addRoleToMember(member, role).complete();
            }
        }
    }

    private void cleanMemberRoles(Member member, Set<Role> roles) throws RoleAccessException {
        var guild = member.getGuild();
        var reputationRoles = guildData.getReputationRoles(guild)
                .stream()
                .map(ReputationRole::roleId)
                .map(guild::getRoleById)
                .filter(Objects::nonNull).toList();
        for (var role : reputationRoles) {
            assertInteract(role, member.getGuild());
            if (roles.contains(role)) continue;
            log.debug("Removing role {} on {}", Roles.prettyName(role), Guilds.prettyName(guild));
            guild.removeRoleFromMember(member, role).complete();
        }
    }

    private void assertInteract(Role role, Guild guild) throws RoleAccessException {
        if (!guild.getSelfMember().canInteract(role)) {
            throw new RoleAccessException(role);
        }
    }

    public Task<Void> updateBatch(Guild guild) {
        log.info("Started batch update for guild {}", Guilds.prettyName(guild));
        return guild.loadMembers(this::update);
    }
}
