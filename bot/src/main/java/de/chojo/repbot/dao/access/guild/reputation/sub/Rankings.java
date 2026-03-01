/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub;

import de.chojo.repbot.dao.access.guild.reputation.Reputation;
import de.chojo.repbot.dao.access.guild.reputation.sub.ranking.GuildGiven;
import de.chojo.repbot.dao.access.guild.reputation.sub.ranking.GuildReceived;
import de.chojo.repbot.dao.access.guild.reputation.sub.ranking.UserRankings;
import de.chojo.repbot.dao.components.GuildHolder;
import net.dv8tion.jda.api.entities.Guild;

public class Rankings implements GuildHolder {
    private final Reputation reputation;
    private final GuildGiven guildGiven = new GuildGiven(this);
    private final GuildReceived guildReceived = new GuildReceived(this);
    private final UserRankings user = new UserRankings(this);

    public Rankings(Reputation reputation) {
        this.reputation = reputation;
    }

    public Reputation reputation() {
        return reputation;
    }

    public GuildGiven given() {
        return guildGiven;
    }

    public GuildReceived received() {
        return guildReceived;
    }

    @Override
    public Guild guild() {
        return reputation.guild();
    }

    @Override
    public long guildId() {
        return reputation.guildId();
    }

    public UserRankings user() {
        return user;
    }
}
