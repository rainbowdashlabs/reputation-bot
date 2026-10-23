/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.reputation.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dataconsistency.TestDatabase;
import de.chojo.repbot.util.QueryLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.INSTANT_TIMESTAMP;

@Tag("database")
class ProfileQueryTest {
    private static final String PROFILE = QueryLoader.loadQuery("repuser", "profile");
    private static TestDatabase database;

    @BeforeAll
    static void setUp() throws IOException, SQLException {
        database = TestDatabase.start();
        database.execute(
                "INSERT INTO guilds(guild_id) VALUES (1)",
                "INSERT INTO reputation_log(guild_id, donor_id, receiver_id, message_id) VALUES (1, 10, 20, 100)",
                "INSERT INTO reputation_log(guild_id, donor_id, receiver_id, message_id) VALUES (1, 10, 20, 101)",
                "INSERT INTO reputation_log(guild_id, donor_id, receiver_id, message_id) VALUES (1, 10, 30, 102)",
                "INSERT INTO reputation_offset(guild_id, user_id, amount) VALUES (1, 20, 5)",
                "INSERT INTO cleanup_schedule(guild_id, user_id) VALUES (1, 20)");
    }

    @AfterAll
    static void tearDown() {
        database.close();
    }

    private static Optional<Long> rank(long userId) {
        return query(PROFILE)
                .single(call().bind("guild_id", 1L)
                        .bind("user_id", userId)
                        .bind("date_init", ReputationMode.TOTAL.dateInit(), INSTANT_TIMESTAMP)
                        .bind("reset_date", (Instant) null, INSTANT_TIMESTAMP))
                .map(row -> row.getLong("rank"))
                .first();
    }

    @Test
    void memberScheduledForCleanupHasNoProfile() {
        Assertions.assertTrue(rank(20).isEmpty());
    }

    @Test
    void membersScheduledForCleanupAreNotRanked() {
        Assertions.assertEquals(Optional.of(1L), rank(30));
    }
}
