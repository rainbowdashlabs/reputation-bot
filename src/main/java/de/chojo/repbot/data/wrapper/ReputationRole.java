package de.chojo.repbot.data.wrapper;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

@Getter
public class ReputationRole {
    private final long roleId;
    private final long reputation;
    private Role role = null;

    public ReputationRole(long roleId, long reputation) {
        this.roleId = roleId;
        this.reputation = reputation;
    }

    public Role getRole(Guild guild) {
        if (role == null) {
            role = guild.getRoleById(roleId);
        }
        return role;
    }
}
