/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub.thanking;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.Set;

public class ChannelsPOJO {
    @JsonSerialize(contentUsing = ToStringSerializer.class)
    protected final Set<Long> channels;

    @JsonSerialize(contentUsing = ToStringSerializer.class)
    protected final Set<Long> categories;

    protected boolean whitelist;

    public ChannelsPOJO(Set<Long> channels, Set<Long> categories, boolean whitelist) {
        this.channels = channels;
        this.categories = categories;
        this.whitelist = whitelist;
    }

    public Set<Long> channelIds() {
        return channels;
    }

    public Set<Long> categoryIds() {
        return categories;
    }

    public boolean isWhitelist() {
        return whitelist;
    }
}
