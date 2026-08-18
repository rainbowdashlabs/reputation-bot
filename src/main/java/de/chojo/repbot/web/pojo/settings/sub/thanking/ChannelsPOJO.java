/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub.thanking;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.Set;

public class ChannelsPOJO {
    @JsonSerialize(contentUsing = ToStringSerializer.class)
    protected final Set<Long> channels;

    @JsonSerialize(contentUsing = ToStringSerializer.class)
    protected final Set<Long> categories;

    protected boolean whitelist;

    /**
     * All values are required. A partial body is rejected instead of silently resetting the omitted settings, because
     * the settings are applied by difference.
     */
    @JsonCreator
    public ChannelsPOJO(
            @JsonProperty(value = "channels", required = true) Set<Long> channels,
            @JsonProperty(value = "categories", required = true) Set<Long> categories,
            @JsonProperty(value = "whitelist", required = true) boolean whitelist) {
        this.channels = channels;
        this.categories = categories;
        this.whitelist = whitelist;
    }

    public Set<Long> channelIds() {
        return channels;
    }

    public Set<Long> copyChannelIds() {
        return Set.copyOf(channels);
    }

    public Set<Long> categoryIds() {
        return categories;
    }

    public Set<Long> copyCategoryIds() {
        return Set.copyOf(categories);
    }

    public boolean isWhitelist() {
        return whitelist;
    }
}
