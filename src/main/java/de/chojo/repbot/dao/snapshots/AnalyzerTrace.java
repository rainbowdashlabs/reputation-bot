/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.snapshots;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a trace of the analyzer, containing the result entry and a list of submit result entries.
 *
 * @param resultEntry the result entry of the analyzer
 * @param submitResultEntries the list of submit result entries
 */
public record AnalyzerTrace(ResultEntry resultEntry, List<SubmitResultEntry> submitResultEntries) {

    /**
     * Builds a list of message embeds representing the analyzer trace.
     *
     * @param guild   the guild where the trace is being built
     * @param context the event context
     * @return a list of message embeds
     */
    public List<MessageEmbed> embed(Guild guild, EventContext context) {
        List<MessageEmbed> embeds = new ArrayList<>();
        if (resultEntry != null) {
            embeds.add(resultEntry.embed(guild, context));
        }
        if (!submitResultEntries.isEmpty()) {
            List<String> events = new ArrayList<>();
            for (var entry : submitResultEntries) {
                var timestamp = TimeFormat.TIME_LONG.format(entry.instant());
                var message = entry.submitResult().type().localeKey();
                events.add("%s %s".formatted(timestamp, context.localize(message, entry.submitResult().replacements()
                                                                                       .toArray(new Replacement[0]))));
            }

            var embed = new LocalizedEmbedBuilder(context.guildLocalizer())
                    .setTitle("Events")
                    .setDescription(String.join("\n", events))
                    .build();
            embeds.add(embed);
        }
        return embeds;
    }
}
