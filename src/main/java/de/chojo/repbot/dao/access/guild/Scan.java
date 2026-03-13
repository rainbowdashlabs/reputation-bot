/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild;

import de.chojo.repbot.dao.components.GuildHolder;
import de.chojo.repbot.service.scanservice.ScanResult;
import de.chojo.repbot.web.pojo.scan.ScanProgress;

import java.time.Instant;
import java.util.Optional;

import static de.chojo.repbot.dao.util.CustomValueConverter.OBJECT_JSON;
import static de.chojo.repbot.dao.util.CustomValueConverter.jsonAdapter;
import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public class Scan implements GuildHolder {
    private final RepGuild repGuild;

    public Scan(RepGuild repGuild) {
        this.repGuild = repGuild;
    }

    public void saveProgress(ScanProgress progress, Instant started, Instant finished) {
        query("""
                INSERT
                INTO
                    scan_results(guild_id, result, started_at, finished_at)
                VALUES
                    (?, ?::JSONB, ?, ?)
                ON CONFLICT(guild_id) DO UPDATE
                    SET
                        result = excluded.result,
                        started_at = excluded.started_at,
                        finished_at = excluded.finished_at
                """)
                .single(call().bind(guildId())
                        .bind(progress, OBJECT_JSON)
                        .bind(started, INSTANT_TIMESTAMP)
                        .bind(finished, INSTANT_TIMESTAMP))
                .insert();
    }

    public Optional<ScanResult> getProgress() {
        return query("""
                SELECT result, started_at, finished_at FROM scan_results WHERE guild_id = ?
                """)
                .single(call().bind(guildId()))
                .map(rs -> new ScanResult(
                        rs.get("result", jsonAdapter(ScanProgress.class)),
                        rs.get("started_at", INSTANT_TIMESTAMP),
                        rs.get("finished_at", INSTANT_TIMESTAMP)))
                .first();
    }

    @Override
    public GuildHolder guildHolder() {
        return repGuild;
    }
}
