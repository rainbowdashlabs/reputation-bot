/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.guild;

import de.chojo.repbot.web.pojo.guild.channel.CategoryPOJO;
import de.chojo.repbot.web.pojo.guild.channel.ChannelPOJO;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChannelViewPOJO {
    List<ChannelPOJO> channels;
    List<CategoryPOJO> categories;

    public ChannelViewPOJO(List<ChannelPOJO> channels, List<CategoryPOJO> categories) {
        this.channels = channels;
        this.categories = categories;
    }

    public static ChannelViewPOJO generate(Guild guild) {
        Map<Long, CategoryPOJO> categories = new LinkedHashMap<>();
        var channels = new LinkedList<ChannelPOJO>();
        guild.getChannels(false).forEach(channel -> {
            if (channel.getType() == ChannelType.CATEGORY) {
                categories.computeIfAbsent(
                        channel.getIdLong(), k -> new CategoryPOJO(channel.getName(), channel.getId()));
            } else if (channel instanceof ICategorizableChannel instance) {
                if (instance.getParentCategory() != null) {
                    Category category = instance.getParentCategory();
                    categories
                            .computeIfAbsent(
                                    category.getIdLong(), k -> new CategoryPOJO(category.getName(), category.getId()))
                            .addChannel(new ChannelPOJO(channel.getName(), channel.getId(), channel.getType()));
                } else {
                    channels.add(new ChannelPOJO(channel.getName(), channel.getId(), channel.getType()));
                }
            }
        });

        return new ChannelViewPOJO(channels, new LinkedList<>(categories.values()));
    }
}
