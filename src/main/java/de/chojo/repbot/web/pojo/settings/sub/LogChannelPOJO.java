/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class LogChannelPOJO {
    @JsonSerialize(using = ToStringSerializer.class)
    protected long channelId;
    protected boolean active;

    public LogChannelPOJO() {
    }

    public LogChannelPOJO(long channelId, boolean active) {
        this.channelId = channelId;
        this.active = active;
    }

    public long channelId() {
        return channelId;
    }

    public boolean active() {
        return active;
    }
}
