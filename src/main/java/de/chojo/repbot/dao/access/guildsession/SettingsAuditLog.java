/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guildsession;

import de.chojo.sadu.mapper.wrapper.Row;

import java.sql.SQLException;
import java.time.Instant;

import static de.chojo.repbot.dao.util.CustomValueConverter.OBJECT_JSON;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public record SettingsAuditLog(
        String settingsKey, long guildId, long memberId, Object oldValue, Object newValue, Instant changed) {

    public static SettingsAuditLog map(Row row) throws SQLException {
        return new SettingsAuditLog(
                row.getString("settings_identifier"),
                row.getLong("guild_id"),
                row.getLong("member_id"),
                row.get("old_value", OBJECT_JSON),
                row.get("new_value", OBJECT_JSON),
                row.get("changed", INSTANT_TIMESTAMP));
    }
}
