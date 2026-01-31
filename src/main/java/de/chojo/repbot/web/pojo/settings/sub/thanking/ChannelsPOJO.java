package de.chojo.repbot.web.pojo.settings.sub.thanking;

import java.util.Set;

public class ChannelsPOJO {
    protected final Set<Long> channels;
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
