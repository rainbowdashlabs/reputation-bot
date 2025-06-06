/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub.ranking;

import de.chojo.repbot.dao.access.guild.reputation.sub.Rankings;
import de.chojo.repbot.dao.access.guild.reputation.sub.ranking.user.UserGiven;
import de.chojo.repbot.dao.access.guild.reputation.sub.ranking.user.UserReceived;
import de.chojo.repbot.dao.components.GuildHolder;
import net.dv8tion.jda.api.entities.Guild;

public class UserRankings implements GuildHolder {
    private final Rankings rankings;
    private final UserReceived userReceived = new UserReceived(this);
    private final UserGiven userGiven = new UserGiven(this);

    public UserRankings(Rankings rankings) {
        this.rankings = rankings;
    }

    @Override
    public Guild guild() {
        return rankings.guild();
    }

    @Override
    public long guildId() {
        return rankings.guildId();
    }

    public Rankings ranking() {
        return rankings;
    }

    public UserReceived received() {
        return userReceived;
    }

    public UserGiven given() {
        return userGiven;
    }
}
