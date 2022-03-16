package de.chojo.repbot.dao.pagination;

public class AnnouncementSettings {
    private boolean active = false;
    private boolean sameChannel = true;
    private long channelId = 0;

    public AnnouncementSettings(boolean active, boolean sameChannel, long channelId) {
        this.active = active;
        this.sameChannel = sameChannel;
        this.channelId = channelId;
    }

    public AnnouncementSettings() {

    }

    public boolean isActive() {
        return active;
    }

    public boolean isSameChannel() {
        return sameChannel;
    }

    public long channelId() {
        return channelId;
    }

    public void active(boolean active) {
        this.active = active;
    }

    public void sameChannel(boolean sameChannel) {
        this.sameChannel = sameChannel;
    }

    public void channelId(long channelId) {
        this.channelId = channelId;
    }
}
