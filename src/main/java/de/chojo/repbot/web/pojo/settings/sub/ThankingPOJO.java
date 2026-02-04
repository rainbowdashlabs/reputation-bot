/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub;

import de.chojo.repbot.web.pojo.settings.sub.thanking.ChannelsPOJO;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ReactionsPOJO;
import de.chojo.repbot.web.pojo.settings.sub.thanking.RolesHolderPOJO;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ThankwordsPOJO;

public class ThankingPOJO {
    ChannelsPOJO channels;
    RolesHolderPOJO donorRoles;
    RolesHolderPOJO receiverRoles;
    ReactionsPOJO reactions;
    ThankwordsPOJO thankwords;

    public ThankingPOJO(
            ChannelsPOJO channels,
            RolesHolderPOJO donorRoles,
            RolesHolderPOJO receiverRoles,
            ReactionsPOJO reactions,
            ThankwordsPOJO thankwords) {
        this.channels = channels;
        this.donorRoles = donorRoles;
        this.receiverRoles = receiverRoles;
        this.reactions = reactions;
        this.thankwords = thankwords;
    }

    public ChannelsPOJO channels() {
        return channels;
    }

    public RolesHolderPOJO donorRoles() {
        return donorRoles;
    }

    public RolesHolderPOJO receiverRoles() {
        return receiverRoles;
    }

    public ReactionsPOJO reactions() {
        return reactions;
    }

    public ThankwordsPOJO thankwords() {
        return thankwords;
    }
}
