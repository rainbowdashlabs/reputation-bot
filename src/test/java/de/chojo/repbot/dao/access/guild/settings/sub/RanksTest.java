/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access.guild.settings.sub;

import de.chojo.repbot.dao.snapshots.ReputationRank;
import de.chojo.repbot.dataconsistency.TestDatabase;
import net.dv8tion.jda.api.entities.Role;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.List;

@Tag("database")
class RanksTest {
    private static TestDatabase database;

    @BeforeAll
    static void setUp() throws IOException, SQLException {
        database = TestDatabase.start();
        database.execute("INSERT INTO guilds(guild_id) VALUES (1), (2), (3)");
    }

    @AfterAll
    static void tearDown() {
        database.close();
    }

    private static Ranks ranks(long guildId) {
        return new Ranks(null) {
            @Override
            public long guildId() {
                return guildId;
            }
        };
    }

    private static Role role(long id) {
        return (Role) Proxy.newProxyInstance(
                Role.class.getClassLoader(), new Class<?>[] {Role.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getIdLong")) return id;
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static List<Long> roleIds(Ranks ranks) {
        return ranks.ranks().stream().map(ReputationRank::roleId).toList();
    }

    @Test
    void addedRankIsListedRightAway() {
        var ranks = ranks(1);
        ranks.add(role(10), 5);
        Assertions.assertEquals(List.of(10L), roleIds(ranks));
        ranks.add(role(11), 10);
        Assertions.assertEquals(List.of(11L, 10L), roleIds(ranks));
    }

    @Test
    void amountIsUsedByOneRoleOnly() {
        var ranks = ranks(2);
        ranks.add(role(20), 5);
        ranks.add(role(21), 5);
        Assertions.assertEquals(List.of(21L), roleIds(ranks));
        Assertions.assertEquals(List.of(21L), roleIds(ranks(2)));
    }

    @Test
    void removedRankIsGoneRightAway() {
        var ranks = ranks(3);
        ranks.add(role(30), 5);
        ranks.ranks();
        ranks.remove(30);
        Assertions.assertEquals(List.of(), roleIds(ranks));
    }
}
