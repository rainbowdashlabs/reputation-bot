/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.provider;

import de.chojo.repbot.dao.access.guildsession.SettingsAuditLog;
import de.chojo.sadu.queries.api.call.Call;

import java.util.List;

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
                .map(e -> Math.floorDiv(e, entries))
                .orElse(0L);
    }
}
