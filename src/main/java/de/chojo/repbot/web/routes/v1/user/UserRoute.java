/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.user;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.provider.UserRepository;
import de.chojo.repbot.dao.provider.VoteRepository;
import de.chojo.repbot.web.routes.RoutesBuilder;

import static io.javalin.apibuilder.ApiBuilder.path;

public class UserRoute implements RoutesBuilder {
    private final UserVoteRoute userVoteRoute;
    private final UserSettingsRoute userSettingsRoute;

    public UserRoute(VoteRepository voteRepository, UserRepository userRepository, Configuration configuration) {
        userVoteRoute = new UserVoteRoute(voteRepository, configuration);
        userSettingsRoute = new UserSettingsRoute(userRepository);
    }

    @Override
    public void buildRoutes() {
        path("user", () -> {
            userVoteRoute.buildRoutes();
            path("settings", userSettingsRoute::buildRoutes);
        });
    }
}
