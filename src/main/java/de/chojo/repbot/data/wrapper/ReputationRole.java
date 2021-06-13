package de.chojo.repbot.data.wrapper;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

public class ReputationRole {
    private final long roleId;
    private final long reputation;
    private Role role;

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

    public long roleId() {
        return roleId;
    }

    public long reputation() {
        return reputation;
    }

    public Role role() {
        return role;
    }
}
