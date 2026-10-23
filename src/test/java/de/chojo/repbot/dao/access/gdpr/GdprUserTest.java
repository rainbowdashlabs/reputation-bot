/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.gdpr;

import de.chojo.repbot.dataconsistency.TestDatabase;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.sql.SQLException;

@Tag("database")
class GdprUserTest {
    private static TestDatabase database;

    @BeforeAll
    static void setUp() throws IOException, SQLException {
        database = TestDatabase.start();
    }

    @AfterAll
    static void tearDown() {
        database.close();
    }

    private static GdprUser user(long id) {
        var user = (User) Proxy.newProxyInstance(
                User.class.getClassLoader(), new Class<?>[] {User.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getIdLong")) return id;
                    throw new UnsupportedOperationException(method.getName());
                });
        return new GdprUser(user);
    }

    @Test
    void firstRequestIsAccepted() {
        Assertions.assertTrue(user(1).request());
    }

    @Test
    void secondRequestWithinThirtyDaysIsRejected() {
        Assertions.assertTrue(user(2).request());
        Assertions.assertFalse(user(2).request());
    }

    @Test
    void requestAfterThirtyDaysIsAccepted() throws SQLException {
        Assertions.assertTrue(user(3).request());
        database.execute("UPDATE gdpr_log SET received = now() - INTERVAL '31 days' WHERE user_id = 3");
        Assertions.assertTrue(user(3).request());
    }
}
