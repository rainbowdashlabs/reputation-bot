package de.chojo.repbot.data.wrapper;

public class RemovalTask {
    private final long taskId;
    private final long guildId;
    private final long userId;

    public RemovalTask(long taskId, long guildId, long userId) {
        this.taskId = taskId;
        this.guildId = guildId;
        this.userId = userId;
    }

    public long taskId() {
        return taskId;
    }

    public long guildId() {
        return guildId;
    }

    public long userId() {
        return userId;
    }
}
