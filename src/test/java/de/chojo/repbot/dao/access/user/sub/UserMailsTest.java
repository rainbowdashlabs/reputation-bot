/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.user.sub;

import de.chojo.repbot.dao.access.user.RepUser;
import de.chojo.repbot.dataconsistency.TestDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Tag("database")
class UserMailsTest {
    private static TestDatabase database;

    @BeforeAll
    static void setUp() throws IOException, SQLException {
        database = TestDatabase.start();
    }

    @AfterAll
    static void tearDown() {
        database.close();
    }

    private static MailEntry entry(long userId, String hash) {
        return new MailEntry(
                userId,
                MailSource.DISCORD,
                hash,
                "u***@example.com",
                true,
                Instant.now().truncatedTo(ChronoUnit.MILLIS),
                "code-" + hash);
    }

    @Test
    void storedMailIsLoadedFromDatabase() {
        MailEntry stored = entry(1, "hash-1");
        new RepUser(1).mails().addMail(stored);

        MailEntry loaded = new RepUser(1).mails().getMail("hash-1").orElseThrow();

        Assertions.assertEquals(stored.userId(), loaded.userId());
        Assertions.assertEquals(stored.source(), loaded.source());
        Assertions.assertEquals(stored.hash(), loaded.hash());
        Assertions.assertEquals(stored.mailShort(), loaded.mailShort());
        Assertions.assertEquals(stored.verified(), loaded.verified());
        Assertions.assertEquals(stored.verificationRequested(), loaded.verificationRequested());
        Assertions.assertEquals(stored.verificationCode(), loaded.verificationCode());
    }

    @Test
    void onlyMailsOfTheUserAreLoaded() {
        new RepUser(2).mails().addMail(entry(2, "hash-2"));
        new RepUser(3).mails().addMail(entry(3, "hash-3"));

        var mails = new RepUser(2).mails().mails();

        Assertions.assertEquals(1, mails.size());
        Assertions.assertTrue(mails.containsKey("hash-2"));
    }

    @Test
    void removedMailIsGone() {
        new RepUser(4).mails().addMail(entry(4, "hash-4"));

        Assertions.assertTrue(new RepUser(4).mails().removeMail("hash-4"));
        Assertions.assertTrue(new RepUser(4).mails().mails().isEmpty());
    }
}
