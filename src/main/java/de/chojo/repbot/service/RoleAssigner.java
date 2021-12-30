package de.chojo.repbot.service;

import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.ReputationData;
import de.chojo.repbot.data.wrapper.ReputationRole;
import de.chojo.repbot.data.wrapper.ReputationUser;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class RoleAssigner {
    private final GuildData guildData;
    private final ReputationData reputationData;
    private final ExecutorService worker;

    public RoleAssigner(DataSource dataSource, ExecutorService worker) {
        guildData = new GuildData(dataSource);
        reputationData = new ReputationData(dataSource);
        this.worker = worker;
    }

    public void update(@Nullable Member member) throws HierarchyException, RoleAccessException {
        if (member == null) return;
        var guild = member.getGuild();
        var reputation = reputationData.getReputation(guild, member.getUser()).orElse(ReputationUser.empty(member.getUser()));
        var settings = guildData.getGuildSettings(member.getGuild());

        var roles = guildData.getCurrentReputationRole(guild, reputation.reputation(), settings.generalSettings().isStackRoles())
                .stream()
                .map(r -> guild.getRoleById(r.roleId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (roles.isEmpty()) return;

        cleanMemberRoles(member, roles);

        if (member.getRoles().containsAll(roles)) return;

        for (var role : roles) {
            assertInteract(role, member.getGuild());
            if (!member.getRoles().contains(role))
                guild.addRoleToMember(member, role).complete();
        }
    }

    private void cleanMemberRoles(Member member, Set<Role> roles) throws RoleAccessException {
        var guild = member.getGuild();
        var reputationRoles = guildData.getReputationRoles(guild)
                .stream()
                .map(ReputationRole::roleId)
                .map(guild::getRoleById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        for (var role : reputationRoles) {
            assertInteract(role, member.getGuild());
            if (roles.contains(role)) continue;
            guild.removeRoleFromMember(member, role).complete();
        }
    }

    private void assertInteract(Role role, Guild guild) throws RoleAccessException {
        if (!guild.getSelfMember().canInteract(role)) {
            throw new RoleAccessException(role);
        }
    }

    public CompletableFuture<Void> updateBatch(Guild guild) {
        return CompletableFuture.runAsync(() -> {
            for (var member : guild.loadMembers().get()) {
                update(member);
            }
        }, worker);
    }
}
