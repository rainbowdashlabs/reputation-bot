package de.chojo.repbot.web.pojo.settings.sub;

public class AnnouncementsPOJO {
    protected boolean active = false;
    protected boolean sameChannel = true;
    protected long channelId = 0;

    public boolean isActive() {
        return active;
    }

    public boolean isSameChannel() {
        return sameChannel;
    }

    public long channelId() {
        return channelId;
    }
}
