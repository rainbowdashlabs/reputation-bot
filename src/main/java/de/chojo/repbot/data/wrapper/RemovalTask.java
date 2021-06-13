package de.chojo.repbot.data.wrapper;

public class RemovalTask {
    private final Long taskId;
    private final Long guildId;
    private final Long userId;

    public RemovalTask(Long taskId, Long guildId, Long userId) {
        this.taskId = taskId;
        this.guildId = guildId;
        this.userId = userId;
    }

    public Long taskId() {
        return taskId;
    }

    public Long guildId() {
        return guildId;
    }

    public Long userId() {
        return userId;
    }
}
