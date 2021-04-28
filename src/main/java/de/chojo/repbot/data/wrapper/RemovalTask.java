package de.chojo.repbot.data.wrapper;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RemovalTask {
    private final Long taskId;
    private final Long guildId;
    private final Long userId;
}
