/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.subscriptions;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;

import java.sql.SQLException;
import java.time.Instant;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

public class TokenPurchase {
    private final long guildId;
    private final int featureId;
    private final Instant expires;
    private boolean autoRenewal;

    public TokenPurchase(long guildId, int featureId, Instant expires, boolean autoRenewal) {
        this.guildId = guildId;
        this.featureId = featureId;
        this.expires = expires;
        this.autoRenewal = autoRenewal;
    }

    @MappingProvider({"guild_id", "feature_id", "expires", "auto_renewal"})
    public TokenPurchase(Row row) throws SQLException {
        this(
                row.getLong("guild_id"),
                row.getInt("feature_id"),
                row.get("expires", INSTANT_TIMESTAMP),
                row.getBoolean("auto_renewal"));
    }

    public void setAutoRenewal(boolean autoRenewal) {
        query("""
                UPDATE token_purchases SET auto_renewal = ? WHERE guild_id = ? AND feature_id = ?;""")
                .single(call().bind(autoRenewal).bind(guildId).bind(featureId))
                .update()
                .ifChanged(i -> this.autoRenewal = autoRenewal);
    }

    public long guildId() {
        return guildId;
    }

    public int featureId() {
        return featureId;
    }

    public Instant expires() {
        return expires;
    }

    public boolean autoRenewal() {
        return autoRenewal;
    }
}
