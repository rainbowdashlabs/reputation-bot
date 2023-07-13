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

public class AnswerResultSnapshot extends DirectResultSnapshot implements ResultSnapshot {
    private final long messageId;

    @JsonCreator
    public AnswerResultSnapshot(@JsonProperty("donorId") long donorId,
                                @JsonProperty("match") String match,
                                @JsonProperty("receivers") List<Long> receivers,
                                @JsonProperty("messageId") long messageId) {
        super(ThankType.ANSWER, donorId, match, receivers);
        this.messageId = messageId;
    }

    @Override
    public void add(Guild guild, ResultEntry entry, LocalizedEmbedBuilder builder) {
        super.add(guild, entry, builder);
        builder.addField("command.log.analyzer.message.field.referenceMessage",
                "[$%s$](%s)".formatted("words.jump", Message.JUMP_URL.formatted(guild.getIdLong(), entry.channelId(), messageId)),
                true);
    }
}
