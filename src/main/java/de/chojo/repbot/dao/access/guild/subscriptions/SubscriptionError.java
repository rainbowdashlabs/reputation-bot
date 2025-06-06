package de.chojo.repbot.dao.access.guild.subscriptions;

import de.chojo.repbot.util.SupporterFeature;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.converter.StandardValueConverter;

import java.sql.SQLException;
import java.time.Instant;

public record SubscriptionError(SupporterFeature type, Instant lastSend, int count) {


    public SubscriptionError(SupporterFeature type, Instant lastSend, int count) {
        this.type = type;
        this.lastSend = lastSend;
        this.count = count;
    }

    public static SubscriptionError build(Row row) throws SQLException {
        return new SubscriptionError(row.getEnum("type", SupporterFeature.class),
                row.get("last_send", StandardValueConverter.INSTANT_TIMESTAMP),
                row.getInt("count"));
    }
}
