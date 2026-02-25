/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.provider;

import de.chojo.repbot.dao.access.guildsession.SettingsAuditLog;
import de.chojo.sadu.queries.api.call.Call;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static de.chojo.repbot.dao.util.CustomValueConverter.OBJECT_JSON;
import static de.chojo.sadu.queries.api.query.Query.query;

public class SettingsAuditLogRepository {
    public List<SettingsAuditLog> getAuditLog(long guildId, int page, int entries) {
        return query("SELECT * FROM settings_audit_log WHERE guild_id = ? ORDER BY changed DESC LIMIT ? OFFSET ?")
                .single(Call.call().bind(guildId).bind(entries).bind(page * entries))
                .map(SettingsAuditLog::map)
                .all();
    }

    public long getAuditLogPages(long guildId, int entries) {
        return query("SELECT count(1) FROM settings_audit_log WHERE guild_id = ?")
                .single(Call.call().bind(guildId))
                .mapAs(Long.class)
                .first()
                .map(e -> (long) Math.ceil((double) e / entries))
                .orElse(0L);
    }

    public void recordChange(long guildId, long userId, String settingsKey, Object oldValue, Object newValue) {
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
                .single(Call.call().bind(guildId).bind(settingsKey))
                .map(SettingsAuditLog::map)
                .first();
        if (last.isPresent()) {
            SettingsAuditLog change = last.get();
            // Settings that were changed in the last 5 minutes are considered one session and are recorded together.
            // Mostly to avoid bloat in the settings audit log.
            if (change.memberId() == userId
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
                        .single(Call.call()
                                .bind(newValue, OBJECT_JSON)
                                .bind("settings_identifier", settingsKey)
                                .bind("guild_id", guildId)
                                .bind("member_id", userId))
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
                .single(Call.call()
                        .bind(guildId)
                        .bind(userId)
                        .bind(settingsKey)
                        .bind(oldValue, OBJECT_JSON)
                        .bind(newValue, OBJECT_JSON))
                .update();
    }
}
