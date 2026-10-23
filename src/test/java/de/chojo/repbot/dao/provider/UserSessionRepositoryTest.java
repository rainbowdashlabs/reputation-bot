/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.provider;

import de.chojo.repbot.dataconsistency.TestDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@Tag("database")
class UserSessionRepositoryTest {
    private static TestDatabase database;
    private final UserSessionRepository repository = new UserSessionRepository();

    @BeforeAll
    static void setUp() throws IOException, SQLException {
        database = TestDatabase.start();
    }

    @AfterAll
    static void tearDown() {
        database.close();
    }

    private static void lastUsedHoursAgo(String token, int hours) throws SQLException {
        database.execute("UPDATE user_session SET last_used = now() - make_interval(hours => %d) WHERE token = '%s'"
                .formatted(hours, token));
    }

    @Test
    void recentlyUsedSessionIsFound() {
        repository.createSession("recent", 1);
        Assertions.assertTrue(repository.byToken("recent", 720).isPresent());
    }

    @Test
    void unusedSessionExpires() throws SQLException {
        repository.createSession("stale", 2);
        lastUsedHoursAgo("stale", 721);
        Assertions.assertTrue(repository.byToken("stale", 720).isEmpty());
    }

    @Test
    void expiredSessionsAreDeleted() throws SQLException {
        repository.createSession("old", 3);
        repository.createSession("fresh", 3);
        lastUsedHoursAgo("old", 800);
        Assertions.assertEquals(1, repository.deleteExpired(720));
        Assertions.assertTrue(repository.byToken("fresh", 720).isPresent());
        Assertions.assertTrue(repository.byToken("old", 100000).isEmpty());
    }
}
