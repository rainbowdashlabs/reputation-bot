/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.guild;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.dao.access.guild.settings.sub.ReputationMode;
import de.chojo.repbot.dao.access.guildsession.GuildSession;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.services.RankingService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public abstract class BaseRankingRoute implements RoutesBuilder {
    protected static final int MAX_PAGE_SIZE = 50;
    protected static final int DEFAULT_PAGE_SIZE = 10;
    protected static final int FREE_MAX_PAGE_SIZE = 20;

    protected final RankingService rankingService;
    protected final Configuration configuration;

    protected BaseRankingRoute(RankingService rankingService, Configuration configuration) {
        this.rankingService = rankingService;
        this.configuration = configuration;
    }

    protected ReputationMode resolveMode(Context ctx, GuildSession session) {
        String modeParam = ctx.queryParam("mode");
        if (modeParam == null) return session.repGuild().settings().general().reputationMode();
        try {
            return ReputationMode.valueOf(modeParam);
        } catch (IllegalArgumentException e) {
            return session.repGuild().settings().general().reputationMode();
        }
    }

    protected boolean requireAdvancedRankings(Context ctx, GuildSession session) {
        if (!session.premiumValidator().features().advancedRankings().unlocked()) {
            ctx.status(HttpStatus.FORBIDDEN);
            return false;
        }
        return true;
    }

    protected int resolvePage(Context ctx, GuildSession session) {
        boolean reputationLogUnlocked =
                session.premiumValidator().features().reputationLog().unlocked();
        int maxPages = reputationLogUnlocked
                ? Integer.MAX_VALUE
                : configuration.skus().features().reputationLog().defaultSize();
        return Math.min(ctx.queryParamAsClass("page", Integer.class).getOrDefault(0), maxPages - 1);
    }

    protected int resolvePageSize(Context ctx, GuildSession session) {
        boolean reputationLogUnlocked =
                session.premiumValidator().features().reputationLog().unlocked();
        int maxPageSize = reputationLogUnlocked ? MAX_PAGE_SIZE : FREE_MAX_PAGE_SIZE;
        return Math.min(ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(DEFAULT_PAGE_SIZE), maxPageSize);
    }
}
