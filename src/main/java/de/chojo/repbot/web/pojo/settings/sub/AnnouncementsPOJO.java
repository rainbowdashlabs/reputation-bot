/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class AnnouncementsPOJO {
    protected boolean active = false;
    protected boolean sameChannel = true;

    @JsonSerialize(using = ToStringSerializer.class)
    protected long channelId = 0;

    public AnnouncementsPOJO() {}

    public AnnouncementsPOJO(boolean active, boolean sameChannel, long channelId) {
        this.active = active;
        this.sameChannel = sameChannel;
        this.channelId = channelId;
    }

    public boolean active() {
        return active;
    }

    public boolean sameChannel() {
        return sameChannel;
    }

    public long channelId() {
        return channelId;
    }
}
