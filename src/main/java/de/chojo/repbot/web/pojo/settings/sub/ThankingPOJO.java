/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ChannelsPOJO;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ReactionsPOJO;
import de.chojo.repbot.web.pojo.settings.sub.thanking.RolesHolderPOJO;
import de.chojo.repbot.web.pojo.settings.sub.thanking.ThankwordsPOJO;

public class ThankingPOJO {
    ChannelsPOJO channels;
    RolesHolderPOJO donorRoles;
    RolesHolderPOJO receiverRoles;
    RolesHolderPOJO denyDonorRoles;
    RolesHolderPOJO denyReceiverRoles;
    ReactionsPOJO reactions;
    ThankwordsPOJO thankwords;

    /**
     * All values are required. A partial body is rejected instead of silently resetting the omitted settings, because
     * the settings are applied by difference.
     */
    @JsonCreator
    public ThankingPOJO(
            @JsonProperty(value = "channels", required = true) ChannelsPOJO channels,
            @JsonProperty(value = "donorRoles", required = true) RolesHolderPOJO donorRoles,
            @JsonProperty(value = "denyDonorRoles", required = true) RolesHolderPOJO denyDonorRoles,
            @JsonProperty(value = "receiverRoles", required = true) RolesHolderPOJO receiverRoles,
            @JsonProperty(value = "denyReceiverRoles", required = true) RolesHolderPOJO denyReceiverRoles,
            @JsonProperty(value = "reactions", required = true) ReactionsPOJO reactions,
            @JsonProperty(value = "thankwords", required = true) ThankwordsPOJO thankwords) {
        this.channels = channels;
        this.donorRoles = donorRoles;
        this.denyDonorRoles = denyDonorRoles;
        this.receiverRoles = receiverRoles;
        this.denyReceiverRoles = denyReceiverRoles;
        this.reactions = reactions;
        this.thankwords = thankwords;
    }

    public ChannelsPOJO channels() {
        return channels;
    }

    public RolesHolderPOJO donorRoles() {
        return donorRoles;
    }

    public RolesHolderPOJO denyDonorRoles() {
        return denyDonorRoles;
    }

    public RolesHolderPOJO denyReceiverRoles() {
        return denyReceiverRoles;
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
