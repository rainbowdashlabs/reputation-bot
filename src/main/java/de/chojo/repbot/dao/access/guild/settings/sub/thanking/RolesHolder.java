package de.chojo.repbot.dao.access.guild.settings.sub.thanking;

import de.chojo.repbot.dao.access.guild.settings.sub.Thanking;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class RolesHolder extends QueryFactoryHolder implements GuildHolder {
    protected final Set<Long> roleIds;
    protected final Thanking thanking;

    public RolesHolder(Thanking thanking, Set<Long> roleIds) {
        super(thanking);
        this.thanking = thanking;
        this.roleIds = roleIds;
    }

    public boolean hasRole(@Nullable Member member) {
        if (member == null) return false;
        if (roleIds.isEmpty()) return true;
        for (var role : member.getRoles()) {
            if (roleIds.contains(role.getIdLong())) return true;
        }
        return false;
    }

    public Set<Role> roles() {
        return roleIds.stream().map(guild()::getRoleById).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public Guild guild() {
        return thanking.guild();
    }

    protected abstract String targetTable();

    public boolean add(Role role) {
        var result = builder().query("INSERT INTO %s(guild_id, role_id) VALUES (?,?) ON CONFLICT(guild_id, role_id) DO NOTHING", targetTable())
                             .paramsBuilder(stmt -> stmt.setLong(guildId()).setLong(role.getIdLong()))
                             .update()
                             .executeSync() > 0;
        if (result) {
            roleIds.add(role.getIdLong());
        }
        return result;
    }

    public boolean remove(Role role) {
        var result = builder().query("DELETE FROM %s WHERE guild_id = ? AND role_id = ?", targetTable())
                             .paramsBuilder(stmt -> stmt.setLong(guildId()).setLong(role.getIdLong()))
                             .update()
                             .executeSync() > 0;
        if (result) {
            roleIds.remove(role.getIdLong());
        }
        return result;
    }
}
