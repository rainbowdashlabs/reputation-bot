/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.provider;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.repbot.dao.access.user.RepUser;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class UserRepository {
    private final Cache<Long, RepUser> users =
            CacheBuilder.newBuilder().expireAfterAccess(15, TimeUnit.MINUTES).build();

    public RepUser byId(long id) {
        try {
            return users.get(id, () -> new RepUser(id));
        } catch (ExecutionException e) {
            return new RepUser(id);
        }
    }
}
