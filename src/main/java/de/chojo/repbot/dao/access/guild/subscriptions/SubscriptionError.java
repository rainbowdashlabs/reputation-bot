/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.subscriptions;

import de.chojo.repbot.util.SupporterFeature;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.converter.StandardValueConverter;

import java.sql.SQLException;
import java.time.Instant;

public record SubscriptionError(SupporterFeature type, Instant created, Instant lastSend, int count, boolean notified) {

    /**
     * Whether the feature has to be reported in the next notice.
     * <p>
     * Blocking features are reported again every interval until they are fixed. Paused features are reported once;
     * only a notice that reached nobody is repeated.
     *
     * @param dueBefore errors last sent before this moment are due
     * @return true if the error is due
     */
    public boolean isDue(Instant dueBefore) {
        if (!type.isBlocking() && notified) return false;
        return lastSend.isBefore(dueBefore);
    }

    public static SubscriptionError build(Row row) throws SQLException {
        return new SubscriptionError(
                row.getEnum("type", SupporterFeature.class),
                row.get("date_inserted", StandardValueConverter.INSTANT_TIMESTAMP),
                row.get("last_send", StandardValueConverter.INSTANT_TIMESTAMP),
                row.getInt("count"),
                row.getBoolean("notified"));
    }
}
