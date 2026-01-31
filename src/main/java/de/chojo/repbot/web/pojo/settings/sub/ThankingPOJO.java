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

    public ThankingPOJO(ChannelsPOJO channels, RolesHolderPOJO donorRoles, RolesHolderPOJO receiverRoles, ReactionsPOJO reactions, ThankwordsPOJO thankwords) {
        this.channels = channels;
        this.donorRoles = donorRoles;
        this.receiverRoles = receiverRoles;
        this.reactions = reactions;
        this.thankwords = thankwords;
    }
}
