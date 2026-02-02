/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.pojo.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshInterval;
import de.chojo.repbot.dao.access.guild.settings.sub.autopost.RefreshType;

public class AutopostPOJO {
    protected boolean active = false;
    protected long channelId = 0;
    protected long messageId = 0;
    protected RefreshType refreshType = RefreshType.DELETE_AND_REPOST;
    protected RefreshInterval refreshInterval = RefreshInterval.DAILY;

    public boolean active() {
        return active;
    }

    public long channelId() {
        return channelId;
    }

    public long messageId() {
        return messageId;
    }

    public RefreshType refreshType() {
        return refreshType;
    }

    public RefreshInterval refreshInterval() {
        return refreshInterval;
    }
}
