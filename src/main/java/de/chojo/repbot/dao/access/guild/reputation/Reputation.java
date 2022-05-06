package de.chojo.repbot.dao.access.guild.reputation;

import de.chojo.repbot.dao.access.guild.RepGuild;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.data.wrapper.GuildReputationStats;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Optional;

public class Reputation extends QueryFactoryHolder implements GuildHolder {
    private final RepGuild repGuild;

    public Reputation(RepGuild repGuild) {
        super(repGuild);
        this.repGuild = repGuild;
    }

    @Override
    public Guild guild() {
        return repGuild.guild();
    }

    public Optional<GuildReputationStats> getGuildReputationStats(Guild guild) {
        return builder(GuildReputationStats.class)
                .query("SELECT total_reputation, week_reputation, today_reputation, top_channel FROM get_guild_stats(?)")
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()))
                .readRow(rs -> new GuildReputationStats(
                        rs.getInt("total_reputation"),
                        rs.getInt("week_reputation"),
                        rs.getInt("today_reputation"),
                        rs.getLong("top_channel")
                )).firstSync();
    }
}
