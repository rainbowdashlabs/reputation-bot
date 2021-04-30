package de.chojo.repbot.data.wrapper;

import de.chojo.repbot.analyzer.ThankType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReputationLogEntry {
    private final long guildId;
    private final long channel_id;
    private final long donorId;
    private final long receiverId;
    private final long messageId;
    private final long ref_message_id;
    private final ThankType type;
}
