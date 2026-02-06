/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
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
        this(
                row.getLong("guild_id"),
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
        Optional<Instant> lastUsed = query(
                        "UPDATE guild_session SET last_used = now() WHERE token = ? RETURNING last_used")
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

    public GuildSession toGuildSession(
            Configuration configuration, ShardManager shardManager, GuildRepository guildRepository) {
        return new GuildSession(configuration, shardManager, guildRepository, this);
    }

    public void recordChange(String settingsKey, Object oldValue, Object newValue) {
        Optional<SettingsAuditLog> last = query("""
                SELECT
                    guild_id,
                    member_id,
                    settings_identifier,
                    old_value,
                    new_value,
                    changed
                FROM
                    settings_audit_log log
                WHERE guild_id = ?
                  AND settings_identifier = ?
                ORDER BY changed DESC
                LIMIT 1
                """)
                .single(call().bind(guildId()).bind(settingsKey))
                .map(SettingsAuditLog::map)
                .first();
        if (last.isPresent()) {
            SettingsAuditLog change = last.get();
            // Settings that were changed in the last 5 minutes are considered one session and are recorded together.
            // Mostly to avoid bloat in the settings audit log.
            if (change.memberId() == memberId()
                    && change.changed().isAfter(Instant.now().minus(5, ChronoUnit.MINUTES))) {
                query("""
                        WITH
                            latest AS (
                                SELECT changed
                                FROM settings_audit_log
                                WHERE guild_id = :guild_id AND member_id = :member_id AND settings_identifier = :settings_identifier
                                ORDER BY changed DESC
                                LIMIT 1
                            )
                        UPDATE settings_audit_log
                        SET
                            new_value = coalesce(?::JSONB, 'null'::JSONB)
                        WHERE settings_identifier = :settings_identifier
                          AND guild_id = :guild_id
                          AND member_id = :member_id
                          AND changed IN (
                            SELECT changed
                            FROM latest
                                         )""")
                        .single(call().bind(newValue, OBJECT_JSON)
                                .bind("settings_identifier", settingsKey)
                                .bind("guild_id", guildId())
                                .bind("member_id", memberId()))
                        .update();
                return;
            }
        }
        query("""
                INSERT
                INTO
                    settings_audit_log
                    (guild_id, member_id, settings_identifier, old_value, new_value)
                VALUES
                    (?, ?, ?, coalesce(?::JSONB, 'null'::JSONB), coalesce(?::JSONB, 'null'::JSONB))""")
                .single(call().bind(guildId())
                        .bind(memberId())
                        .bind(settingsKey)
                        .bind(oldValue, OBJECT_JSON)
                        .bind(newValue, OBJECT_JSON))
                .update();
    }
}
