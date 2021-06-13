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
import java.util.stream.Collectors;

public class RoleAssigner {
    private final GuildData guildData;
    private final ReputationData reputationData;

    public RoleAssigner(DataSource dataSource) {
        guildData = new GuildData(dataSource);
        reputationData = new ReputationData(dataSource);
    }

    public void update(@Nullable Member member) throws HierarchyException, RoleAccessException {
        if (member == null) return;
        var guild = member.getGuild();
        var reputation = reputationData.getReputation(guild, member.getUser()).orElse(ReputationUser.empty(member.getUser()));

        var optRepRole = guildData.getCurrentReputationRole(guild, reputation.reputation());

        if (optRepRole.isEmpty()) return;

        var repRole = optRepRole.get();

        var roleById = guild.getRoleById(repRole.roleId());

        if (roleById == null) return;

        if (member.getRoles().contains(roleById)) return;

        cleanMemberRoles(member);

        assertInteract(roleById, member.getGuild());

        guild.addRoleToMember(member, roleById).queue();
    }

    private void cleanMemberRoles(Member member) throws RoleAccessException {
        var guild = member.getGuild();
        var collect = guildData.getReputationRoles(guild)
                .stream()
                .map(ReputationRole::roleId)
                .map(guild::getRoleById)
                .filter(Objects::nonNull).collect(Collectors.toList());
        for (var role : collect) {
            assertInteract(role, member.getGuild());
            guild.removeRoleFromMember(member, role).queue();
        }
    }

    private void assertInteract(Role role, Guild guild) throws RoleAccessException {
        if (!guild.getSelfMember().canInteract(role)) {
            throw new RoleAccessException(role);
        }
    }
}
