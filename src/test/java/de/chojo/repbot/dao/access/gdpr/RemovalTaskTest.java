/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.gdpr;

import de.chojo.repbot.dataconsistency.TestDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@Tag("database")
class RemovalTaskTest {
    private static final long USER = 10;
    private static final long OTHER_USER = 11;
    private static final long GUILD = 20;
    private static TestDatabase database;

    @BeforeAll
    static void setUp() throws IOException, SQLException {
        database = TestDatabase.start();
    }

    @AfterAll
    static void tearDown() {
        database.close();
    }

    @Test
    void removesUserSettingsVoteTokensAndAuditLog() throws SQLException {
        for (long user : new long[] {USER, OTHER_USER}) {
            database.execute(
                    "INSERT INTO user_settings(id, vote_guild) VALUES (%d, %d)".formatted(user, GUILD),
                    "INSERT INTO vote_token(entity_id, entity_type, token, total_token) VALUES (%d, 'USER', 5, 5)"
                            .formatted(user),
                    """
                    INSERT INTO settings_audit_log(guild_id, member_id, settings_identifier, old_value, new_value)
                    VALUES (%d, %d, 'general.stackroles', 'false', 'true')""".formatted(GUILD, user));
        }
        database.execute(
                "INSERT INTO vote_token(entity_id, entity_type, token, total_token) VALUES (%d, 'GUILD', 3, 3)"
                        .formatted(USER),
                "INSERT INTO gdpr_log(user_id) VALUES (%d)".formatted(USER));

        RemovalTask.anonymExecute(0, USER);

        Assertions.assertEquals(0, count("SELECT count(1) FROM user_settings WHERE id = " + USER));
        Assertions.assertEquals(
                0, count("SELECT count(1) FROM vote_token WHERE entity_type = 'USER' AND entity_id = " + USER));
        Assertions.assertEquals(0, count("SELECT count(1) FROM settings_audit_log WHERE member_id = " + USER));

        Assertions.assertEquals(
                1, count("SELECT count(1) FROM vote_token WHERE entity_type = 'GUILD' AND entity_id = " + USER));
        Assertions.assertEquals(1, count("SELECT count(1) FROM gdpr_log WHERE user_id = " + USER));
        Assertions.assertEquals(1, count("SELECT count(1) FROM user_settings WHERE id = " + OTHER_USER));
        Assertions.assertEquals(1, count("SELECT count(1) FROM vote_token WHERE entity_id = " + OTHER_USER));
        Assertions.assertEquals(1, count("SELECT count(1) FROM settings_audit_log WHERE member_id = " + OTHER_USER));
    }

    private static long count(String sql) throws SQLException {
        try (var conn = database.dataSource().getConnection();
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
