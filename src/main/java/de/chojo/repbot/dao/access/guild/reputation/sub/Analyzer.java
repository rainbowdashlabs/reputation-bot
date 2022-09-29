package de.chojo.repbot.dao.access.guild.reputation.sub;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.chojo.repbot.analyzer.results.Result;
import de.chojo.repbot.dao.snapshots.analyzer.ResultSnapshot;
import de.chojo.repbot.dao.access.guild.reputation.Reputation;
import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.dao.snapshots.ResultEntry;
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

    public Result log(Message message, Result result) {
        String resultString;
        try {
            resultString = MAPPER.writeValueAsString(result.toSnapshot());
        } catch (JsonProcessingException e) {
            log.error("Could not serialize result", e);
            return result;
        }
        builder().query("""
                        INSERT INTO analyzer_results(guild_id, channel_id, message_id, result) VALUES(?,?,?,?::JSONB)
                            ON CONFLICT (guild_id, message_id)
                                DO NOTHING;
                        """).parameter(stmt -> stmt.setLong(message.getGuild().getIdLong())
                                                   .setLong(message.getChannel().getIdLong())
                                                   .setLong(message.getIdLong())
                                                   .setString(resultString))
                 .insert()
                 .send();
        return result;
    }

    public Optional<ResultEntry> get(long messageId) {
        return builder(ResultEntry.class)
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
