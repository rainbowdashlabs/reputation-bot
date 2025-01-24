/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.repbot.dao.snapshots.analyzer.ResultSnapshot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * Represents an entry in the result snapshot.
 *
 * @param result the result snapshot
 * @param channelId the ID of the channel
 * @param messageId the ID of the message
 */
public record ResultEntry(ResultSnapshot result, long channelId, long messageId) {

    /**
     * Creates an embed message for the result entry.
     *
     * @param guild the guild
     * @param context the event context
     * @return the created message embed
     */
    public MessageEmbed embed(Guild guild, EventContext context) {
        var builder = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setAuthor("command.log.analyzer.message.author",
                        Message.JUMP_URL.formatted(guild.getIdLong(), channelId, messageId),
                        Replacement.create("ID", messageId()));
        result.add(guild, this, builder);
        return builder.build();
    }
}
