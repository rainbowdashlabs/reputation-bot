/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.guild.channel;

import java.util.LinkedList;
import java.util.List;

public class CategoryPOJO {
    protected List<ChannelPOJO> channels;
    String name;
    long id;

    public CategoryPOJO(String name, long id) {
        this.name = name;
        this.id = id;
        channels = new LinkedList<>();
    }

    public void addChannel(ChannelPOJO channel) {
        channels.add(channel);
    }
}
