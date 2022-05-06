package de.chojo.repbot.dao.access.guild.settings.sub;

import de.chojo.repbot.dao.access.guild.reputation.sub.RepUser;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.dao.snapshots.ReputationRank;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class Ranks extends QueryFactoryHolder implements GuildHolder {


    private final LinkedHashSet<ReputationRank> ranks = new LinkedHashSet<>();
    private final Settings settings;
    private final AtomicBoolean stackRoles;

    public Ranks(Settings settings, AtomicBoolean stackRoles) {
        super(settings);
        this.settings = settings;
        this.stackRoles = stackRoles;
    }

    public static Ranks build(Settings settings, AtomicBoolean stackRoles) {
        return new Ranks(settings, stackRoles);
    }

    /**
     * Add a reputation rank.
     * <p>
     * If the role or the reputation amount is already in use it will be removed first.
     *
     * @param role       role
     * @param reputation required reputation of role
     * @return true if the role was added or updated
     */
    public boolean add(Role role, long reputation) {
        var result = builder()
                             .query("""
                                     DELETE FROM
                                         guild_ranks
                                     WHERE
                                         guild_id = ?
                                             AND (role_id = ?
                                                 OR reputation = ?);
                                     """)
                             .paramsBuilder(stmt -> stmt.setLong(guildId()).setLong(role.getIdLong()).setLong(reputation))
                             .append()
                             .query("""
                                     INSERT INTO guild_ranks(guild_id, role_id, reputation) VALUES(?,?,?)
                                         ON CONFLICT(guild_id, role_id)
                                             DO UPDATE
                                                 SET reputation = excluded.reputation,
                                                     role_id = excluded.role_id;
                                     """)
                             .paramsBuilder(stmt -> stmt.setLong(guildId()).setLong(role.getIdLong()).setLong(reputation))
                             .update().executeSync() > 0;
        if (result) {
            ranks.removeIf(r -> r.roleId() == role.getIdLong() || reputation == r.reputation());
            ranks.add(new ReputationRank(this, role.getIdLong(), reputation));
        }
        return result;
    }

    /**
     * Remove a reputation role.
     *
     * @param role role
     * @return true
     */
    public boolean remove(Role role) {
        return builder()
                       .query("DELETE FROM guild_ranks WHERE guild_id = ? AND role_id = ?;")
                       .paramsBuilder(stmt -> stmt.setLong(guildId()).setLong(role.getIdLong()))
                       .update().executeSync() > 0;
    }

    public List<ReputationRank> ranks() {
        if (!ranks.isEmpty()) {
            return ranks.stream().sorted().toList();
        }
        var ranks = builder(ReputationRank.class)
                .query("""
                        SELECT
                            role_id,
                            reputation
                        FROM
                            guild_ranks
                        WHERE guild_id = ?
                        ORDER BY reputation;
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guildId()))
                .readRow(r -> ReputationRank.build(this, r))
                .allSync();
        this.ranks.addAll(ranks);
        return this.ranks.stream().sorted().toList();
    }

    public List<ReputationRank> currentRanks(RepUser user) {
        var profile = user.profile();
        return ranks.stream()
                .filter(rank -> rank.reputation() <= profile.reputation())
                .sorted()
                .limit(stackRoles.get() ? Integer.MAX_VALUE : 1)
                .toList();
    }

    public List<ReputationRank> currentRank(RepUser user) {
        var profile = user.profile();
        return ranks.stream()
                .filter(rank -> rank.reputation() <= profile.reputation())
                .sorted()
                .limit(1)
                .toList();
    }

    public Optional<ReputationRank> nextRank(RepUser user) {
        var profile = user.profile();
        return ranks.stream().filter(rank -> rank.reputation() > profile.reputation()).sorted().limit(1).findFirst();
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }
}
