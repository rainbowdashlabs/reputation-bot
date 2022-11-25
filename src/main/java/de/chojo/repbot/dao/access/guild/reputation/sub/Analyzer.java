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
import de.chojo.sadu.base.QueryFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class Analyzer extends QueryFactory implements GuildHolder {
    private static final Logger log = getLogger(Analyzer.class);
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .configure(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS, true)
            .build()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    private final Reputation reputation;

    public Analyzer(Reputation reputation) {
        super(reputation);
        this.reputation = reputation;
    }

    public AnalyzerResult log(Message message, AnalyzerResult analyzerResult) {
        String resultString;
        try {
            resultString = MAPPER.writeValueAsString(analyzerResult.toSnapshot());
        } catch (JsonProcessingException e) {
            log.error("Could not serialize result", e);
            return analyzerResult;
        }
        builder().query("""
                        INSERT INTO analyzer_results(guild_id, channel_id, message_id, result) VALUES(?, ?, ?, ?::jsonb)
                            ON CONFLICT (guild_id, message_id)
                                DO NOTHING;
                        """).parameter(stmt -> stmt.setLong(message.getGuild().getIdLong())
                                                   .setLong(message.getChannel().getIdLong())
                                                   .setLong(message.getIdLong())
                                                   .setString(resultString))
                 .insert()
                 .send();
        return analyzerResult;
    }

    public void log(Message message, SubmitResult result) {
        String resultString;
        try {
            resultString = MAPPER.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            log.error("Could not serialize result", e);
            return;
        }
        builder().query("""
                        INSERT INTO reputation_results(guild_id, channel_id, message_id, result) VALUES(?, ?, ?, ?::jsonb);
                        """).parameter(stmt -> stmt.setLong(message.getGuild().getIdLong())
                                                   .setLong(message.getChannel().getIdLong())
                                                   .setLong(message.getIdLong())
                                                   .setString(resultString))
                 .insert()
                 .send();
    }

    public Optional<AnalyzerTrace> get(long messageId) {
        var resultEntry = builder(ResultEntry.class)
                .query("SELECT * FROM analyzer_results WHERE guild_id = ? AND message_id = ?;")
                .parameter(stmt -> stmt.setLong(guildId()).setLong(messageId))
                .readRow(row -> {
                    ResultSnapshot result;
                    try {
                        result = MAPPER.readValue(row.getString("result"), ResultSnapshot.class);
                    } catch (JsonProcessingException e) {
                        log.error("Could not deserialize result", e);
                        throw new SQLException(e);
                    }
                    return new ResultEntry(result, row.getLong("channel_id"), messageId);
                }).firstSync();

        var submitResults = builder(SubmitResultEntry.class)
                .query("SELECT guild_id, channel_id, message_id, result, submitted FROM reputation_results WHERE guild_id = ? AND message_id = ? ORDER BY submitted;")
                .parameter(stmt -> stmt.setLong(guildId()).setLong(messageId))
                .readRow(row -> {
                    SubmitResult result;
                    try {
                        result = MAPPER.readValue(row.getString("result"), SubmitResult.class);
                    } catch (JsonProcessingException e) {
                        log.error("Could not deserialize result", e);
                        throw new SQLException(e);
                    }
                    return new SubmitResultEntry(result, row.getLong("channel_id"), messageId, row.getTimestamp("submitted").toInstant());
                }).allSync();

        if(submitResults.isEmpty() && resultEntry.isEmpty()) return Optional.empty();


        return Optional.of(new AnalyzerTrace(resultEntry.orElse(null), submitResults));
    }

    @Override
    public Guild guild() {
        return reputation.guild();
    }

    @Override
    public long guildId() {
        return reputation.guildId();
    }
}
