/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub.ranking;

import de.chojo.repbot.dao.access.guild.reputation.sub.Ranking;
import de.chojo.repbot.dao.access.guild.reputation.sub.ranking.user.UserGiven;
import de.chojo.repbot.dao.access.guild.reputation.sub.ranking.user.UserReceived;
import de.chojo.repbot.dao.components.GuildHolder;
import net.dv8tion.jda.api.entities.Guild;

public class UserRanking implements GuildHolder {
    private final Ranking ranking;
    private final UserReceived userReceived = new UserReceived(this);
    private final UserGiven userGiven = new UserGiven(this);

    public UserRanking(Ranking ranking) {
        this.ranking = ranking;
    }

    @Override
    public Guild guild() {
        return ranking.guild();
    }

    @Override
    public long guildId() {
        return ranking.guildId();
    }

    public Ranking ranking() {
        return ranking;
    }

    public UserReceived received() {
        return userReceived;
    }

    public UserGiven given() {
        return userGiven;
    }
}
