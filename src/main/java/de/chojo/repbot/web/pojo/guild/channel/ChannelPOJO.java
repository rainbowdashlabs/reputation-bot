/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.guild.channel;

import net.dv8tion.jda.api.entities.channel.ChannelType;

public class ChannelPOJO {
    String name;
    String id;
    ChannelType type;

    public ChannelPOJO(String name, String id, ChannelType type) {
        this.name = name;
        this.id = id;
        this.type = type;
    }
}
