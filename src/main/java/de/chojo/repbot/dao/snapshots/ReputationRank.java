package de.chojo.repbot.dao.snapshots;

import de.chojo.repbot.dao.access.guild.settings.sub.Ranks;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.wrapper.util.Row;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

/**
 * Representing a repuration rank.
 * <p>
 * A rank is {@link Comparable} and will be sorted from highest reputation to lowest.
 */
public class ReputationRank extends QueryFactory implements GuildHolder, Comparable<ReputationRank> {
    private final long roleId;
    private final long reputation;
    private final Ranks ranks;
    private Role role;

    public ReputationRank(Ranks ranks, long roleId, long reputation) {
        super(ranks);
        this.ranks = ranks;
        this.roleId = roleId;
        this.reputation = reputation;
    }

    public static ReputationRank build(Ranks ranks, Row rs) throws SQLException {
        return new ReputationRank(ranks,
                rs.getLong("role_id"),
                rs.getLong("reputation")
        );
    }

    @Nullable
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

    /**
     * Remove a reputation role.
     *
     * @return true
     */
    public boolean remove() {
        var success = builder()
                .query("DELETE FROM guild_ranks WHERE guild_id = ? AND role_id = ?;")
                .parameter(stmt -> stmt.setLong(guildId()).setLong(roleId))
                .update()
                .sendSync()
                .changed();
        ranks.refresh();
        return success;
    }

    @Override
    public Guild guild() {
        return ranks.guild();
    }

    @Override
    public int compareTo(@NotNull ReputationRank o) {
        return Long.compare(o.reputation, reputation);
    }

    @Override
    public String toString() {
        return "ReputationRank{" +
               "roleId=" + roleId +
               ", reputation=" + reputation +
               '}';
    }
}
