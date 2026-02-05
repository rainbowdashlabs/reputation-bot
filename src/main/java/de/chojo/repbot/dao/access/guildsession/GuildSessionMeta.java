package de.chojo.repbot.dao.access.guildsession;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.GuildRepository;
import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static de.chojo.repbot.dao.util.CustomValueConverter.OBJECT_JSON;
import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public final class GuildSessionMeta {
    private final long guildId;
    private final long memberId;
    private final String token;
    private final Instant created;
    private Instant lastUsed;

    @MappingProvider({"guild_id", "member_id", "token", "created", "last_used"})
    public GuildSessionMeta(Row row) throws SQLException {
        this(row.getLong("guild_id"),
                row.getLong("member_id"),
                row.getString("token"),
                row.get("created", INSTANT_TIMESTAMP),
                row.get("last_used", INSTANT_TIMESTAMP));
    }

    public GuildSessionMeta(long guildId, long memberId, String token, Instant created, Instant lastUsed) {
        this.guildId = guildId;
        this.memberId = memberId;
        this.token = token;
        this.created = created;
        this.lastUsed = lastUsed;
    }

    public long guildId() {
        return guildId;
    }

    public long memberId() {
        return memberId;
    }

    public String token() {
        return token;
    }

    public Instant created() {
        return created;
    }

    public Instant lastUsed() {
        return lastUsed;
    }

    public void used() {
        Optional<Instant> lastUsed = query("UPDATE guild_session SET last_used = now() WHERE token = ? RETURNING last_used")
                .single(call().bind(token))
                .map(row -> row.get("last_used", INSTANT_TIMESTAMP))
                .first();
        lastUsed.ifPresent(used -> this.lastUsed = used);
    }

    public void delete() {
        query("DELETE FROM guild_session WHERE token = ?")
                .single(call().bind(token))
                .delete();
    }

    public GuildSession toGuildSession(Configuration configuration, ShardManager shardManager, GuildRepository guildRepository) {
        return new GuildSession(configuration, shardManager, guildRepository, this);
    }

    public void recordChange(String settingsKey, Object oldValue, Object newValue) {
        Optional<SettingsAuditLog> last = query("SELECT * FROM settings_audit_log WHERE guild_id = ? AND settings_identifier = ? ORDER BY changed DESC LIMIT 1")
                .single(call().bind(guildId()).bind(settingsKey))
                .mapAs(SettingsAuditLog.class)
                .first();
        if (last.isPresent()) {
            SettingsAuditLog change = last.get();
            if (change.memberId() == memberId() && change.changed().isAfter(Instant.now().minus(5, ChronoUnit.MINUTES))) {
                query("UPDATE settings_audit_log SET old_value = ?, new_value = ? WHERE settings_identifier = ? AND guild_id = ? AND member_id = ?")
                        .single(call().bind(oldValue, OBJECT_JSON).bind(newValue, OBJECT_JSON).bind(settingsKey).bind(guildId()).bind(memberId()))
                        .update();
                return;
            }
        }
        query("INSERT INTO settings_audit_log (guild_id, member_id, settings_identifier, old_value, new_value) VALUES (?, ?, ?, ?, ?)")
                .single(call().bind(guildId()).bind(memberId()).bind(settingsKey).bind(oldValue, OBJECT_JSON).bind(newValue, OBJECT_JSON))
                .update();

    }
}
