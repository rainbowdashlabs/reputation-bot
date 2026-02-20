/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo;

import de.chojo.repbot.web.pojo.guild.GuildPOJO;
import net.dv8tion.jda.api.entities.Guild;

public class GuildSessionPOJO {
    GuildPOJO guild;

    public GuildSessionPOJO(GuildPOJO guild) {
        this.guild = guild;
    }

    public static GuildSessionPOJO generate(Guild guild) {
        return new GuildSessionPOJO(GuildPOJO.generate(guild));
    }
}
