/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.chojo.repbot.analyzer.results.AnalyzerResult;
import de.chojo.repbot.dao.access.guild.reputation.Reputation;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.dao.snapshots.AnalyzerTrace;
import de.chojo.repbot.dao.snapshots.ResultEntry;
import de.chojo.repbot.dao.snapshots.SubmitResultEntry;
import de.chojo.repbot.dao.snapshots.analyzer.ResultSnapshot;
import de.chojo.repbot.service.reputation.SubmitResult;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Class for analyzing and logging reputation-related data.
 */
public class Analyzer implements GuildHolder {
    private static final Logger log = getLogger(Analyzer.class);

    /**
     * ObjectMapper instance for JSON serialization and deserialization.
     */
    public static final ObjectMapper MAPPER = JsonMapper.builder()
                                                        .configure(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS, true)
                                                        .build()
                                                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                                        .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                                                        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    private final Reputation reputation;

    /**
     * Constructs an Analyzer with the specified Reputation instance.
     *
     * @param reputation the Reputation instance
     */
    public Analyzer(Reputation reputation) {
        this.reputation = reputation;
    }

    /**
     * Logs the analyzer result for a given message.
     *
     * @param message the message
     * @param analyzerResult the analyzer result
     * @return the analyzer result
     */
    public AnalyzerResult log(Message message, AnalyzerResult analyzerResult) {
        String resultString;
        try {
            resultString = MAPPER.writeValueAsString(analyzerResult.toSnapshot());
        } catch (JsonProcessingException e) {
            log.error("Could not serialize result", e);
            return analyzerResult;
        }
        query("""
                INSERT INTO analyzer_results(guild_id, channel_id, message_id, result) VALUES(?, ?, ?, ?::JSONB)
                    ON CONFLICT (guild_id, message_id)
                        DO NOTHING;
                """).single(call().bind(message.getGuild().getIdLong())
                                  .bind(message.getChannel().getIdLong())
                                  .bind(message.getIdLong())
                                  .bind(resultString))
                    .insert();
        return analyzerResult;
    }

    /**
     * Logs the submit result for a given message.
     *
     * @param message the message
     * @param result the submit result
     */
    public void log(Message message, SubmitResult result) {
        String resultString;
        try {
            resultString = MAPPER.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            log.error("Could not serialize result", e);
            return;
        }
        query("""
                         INSERT INTO reputation_results(guild_id, channel_id, message_id, result) VALUES(?, ?, ?, ?::JSONB);
                         """).single(call().bind(message.getGuild().getIdLong())
                .bind(message.getChannel().getIdLong())
                                                    .bind(message.getIdLong())
                                                    .bind(resultString))
                 .insert();
    }

    /**
     * Retrieves the analyzer trace for a given message ID.
     *
     * @param messageId the message ID
     * @return an optional containing the analyzer trace if found, otherwise empty
     */
    public Optional<AnalyzerTrace> get(long messageId) {
        var resultEntry = getResults(messageId);

        var submitResults = getSubmitResults(messageId);

        if (submitResults.isEmpty() && resultEntry.isEmpty()) return Optional.empty();

        return Optional.of(new AnalyzerTrace(resultEntry.orElse(null), submitResults));
    }

    /**
     * Retrieves the result entry for a given message ID.
     *
     * @param messageId the message ID
     * @return an optional containing the result entry if found, otherwise empty
     */
    private Optional<ResultEntry> getResults(long messageId) {
        return query("""
                        SELECT guild_id, channel_id, message_id, result, analyzed
                        FROM analyzer_results
                        WHERE guild_id = ?
                          AND message_id = ?;""")
                .single(call().bind(guildId()).bind(messageId))
                .map(row -> {
            ResultSnapshot result;
            try {
                result = MAPPER.readValue(row.getString("result"), ResultSnapshot.class);
            } catch (JsonProcessingException e) {
                log.error("Could not deserialize result", e);
                throw new SQLException(e);
            }
            return new ResultEntry(result, row.getLong("channel_id"), messageId);
        }).first();
    }

    /**
     * Retrieves the submit result entries for a given message ID.
     *
     * @param messageId the message ID
     * @return a list of submit result entries
     */
    private List<SubmitResultEntry> getSubmitResults(long messageId) {
        return query("""
                        SELECT guild_id, channel_id, message_id, result, submitted
                        FROM reputation_results
                        WHERE guild_id = ?
                          AND message_id = ?
                        ORDER BY submitted;""")
                .single(call().bind(guildId()).bind(messageId))
                .map(row -> {
            SubmitResult result;
            try {
                result = MAPPER.readValue(row.getString("result"), SubmitResult.class);
            } catch (JsonProcessingException e) {
                log.error("Could not deserialize result", e);
                throw new SQLException(e);
            }
            return new SubmitResultEntry(result, row.getLong("channel_id"), messageId, row.getTimestamp("submitted").toInstant());
        }).all();
    }

    /**
     * Returns the guild associated with this analyzer.
     *
     * @return the guild
     */
    @Override
    public Guild guild() {
        return reputation.guild();
    }

    /**
     * Returns the guild ID associated with this analyzer.
     *
     * @return the guild ID
     */
    @Override
    public long guildId() {
        return reputation.guildId();
    }
}
