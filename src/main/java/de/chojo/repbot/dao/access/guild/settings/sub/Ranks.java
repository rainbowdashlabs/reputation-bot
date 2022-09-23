package de.chojo.repbot.dao.access.guild.settings.sub;

import de.chojo.repbot.dao.access.guild.reputation.sub.RepUser;
import de.chojo.repbot.dao.access.guild.settings.Settings;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.dao.snapshots.ReputationRank;
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class Ranks extends QueryFactory implements GuildHolder {
    private final LinkedHashSet<ReputationRank> ranks = new LinkedHashSet<>();
    private final Settings settings;
    private final AtomicBoolean stackRoles;

    public Ranks(Settings settings, AtomicBoolean stackRoles) {
        super(settings);
        this.settings = settings;
        this.stackRoles = stackRoles;
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
                .parameter(stmt -> stmt.setLong(guildId()).setLong(role.getIdLong())
                                       .setLong(reputation))
                .append()
                .query("""
                       INSERT INTO guild_ranks(guild_id, role_id, reputation) VALUES(?,?,?)
                           ON CONFLICT(guild_id, role_id)
                               DO UPDATE
                                   SET reputation = excluded.reputation,
                                       role_id = excluded.role_id;
                       """)
                .parameter(stmt -> stmt.setLong(guildId()).setLong(role.getIdLong())
                                       .setLong(reputation))
                .update()
                .sendSync()
                .changed();
        if (result) {
            ranks.removeIf(r -> r.roleId() == role.getIdLong() || reputation == r.reputation());
            ranks.add(new ReputationRank(this, role.getIdLong(), reputation));
        }
        return result;
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
                .parameter(stmt -> stmt.setLong(guildId()))
                .readRow(r -> ReputationRank.build(this, r))
                .allSync();
        this.ranks.addAll(ranks);
        return this.ranks.stream().sorted().toList();
    }

    /**
     * Gets all reputation ranks which should be assigned to the user.
     * <p>
     * This will always contain zero or one role when {@link General#stackRoles()} is true.
     * <p>
     * This will contain up to {@link #ranks()}.size() when {@link General#stackRoles()} is false.
     *
     * @param user user to check
     * @return list of ranks
     */
    public List<ReputationRank> currentRanks(RepUser user) {
        var profile = user.profile();
        return ranks().stream()
                      .filter(rank -> rank.reputation() <= profile.reputation())
                      .sorted()
                      .limit(stackRoles.get() ? Integer.MAX_VALUE : 1)
                      .toList();
    }

    public Optional<ReputationRank> currentRank(RepUser user) {
        var profile = user.profile();
        return ranks().stream()
                      .filter(rank -> rank.reputation() <= profile.reputation())
                      .sorted()
                      .limit(1)
                      .findFirst();
    }

    public Optional<ReputationRank> nextRank(RepUser user) {
        var profile = user.profile();
        return ranks().stream().filter(rank -> rank.reputation() > profile.reputation())
                      .sorted(Comparator.reverseOrder()).limit(1).findFirst();
    }

    @Override
    public Guild guild() {
        return settings.guild();
    }

    public Optional<ReputationRank> rank(Role role) {
        return builder(ReputationRank.class)
                .query("SELECT reputation FROM guild_ranks WHERE guild_id = ? AND role_id = ?")
                .parameter(p -> p.setLong(guildId()).setLong(role.getIdLong()))
                .readRow(row -> new ReputationRank(this, role.getIdLong(), row.getInt("reputation")))
                .firstSync();
    }

    public void refresh() {
        ranks.clear();
    }
}
