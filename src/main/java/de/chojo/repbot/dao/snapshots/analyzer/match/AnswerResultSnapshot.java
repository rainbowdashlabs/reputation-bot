/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots.analyzer.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.repbot.analyzer.results.match.ThankType;
import de.chojo.repbot.dao.snapshots.ResultEntry;
import de.chojo.repbot.dao.snapshots.analyzer.ResultSnapshot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

/**
 * Snapshot class for storing the result of an answer match.
 */
public class AnswerResultSnapshot extends DirectResultSnapshot implements ResultSnapshot {
    private final long messageId;

    /**
     * Constructs an AnswerResultSnapshot instance.
     *
     * @param donorId   the ID of the donor
     * @param match     the match string
     * @param receivers the list of receiver IDs
     * @param messageId the ID of the message
     */
    @JsonCreator
    public AnswerResultSnapshot(@JsonProperty("donorId") long donorId,
                                @JsonProperty("match") String match,
                                @JsonProperty("receivers") List<Long> receivers,
                                @JsonProperty("messageId") long messageId) {
        super(ThankType.ANSWER, donorId, match, receivers);
        this.messageId = messageId;
    }

    /**
     * Adds the result entry to the localized embed builder.
     *
     * @param guild   the guild
     * @param entry   the result entry
     * @param builder the localized embed builder
     */
    @Override
    public void add(Guild guild, ResultEntry entry, LocalizedEmbedBuilder builder) {
        super.add(guild, entry, builder);
        builder.addField("command.log.analyzer.message.field.referenceMessage",
                "[$%s$](%s)".formatted("words.jump", Message.JUMP_URL.formatted(guild.getIdLong(), entry.channelId(), messageId)),
                true);
    }
}
