/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.web.routes.v1.settings;

import de.chojo.jdautil.interactions.dispatching.InteractionHub;
import de.chojo.repbot.dao.provider.SettingsAuditLogRepository;
import de.chojo.repbot.service.AutopostService;
import de.chojo.repbot.service.RoleAssigner;
import de.chojo.repbot.web.cache.MemberCache;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.routes.v1.settings.sub.AbuseProtectionRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.AnnouncementsRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.AuditLogRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.AutopostRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.GeneralRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.IntegrationBypassRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.LogChannelRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.MessagesRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.ProfileRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.RanksRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.ReputationRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.ThankingRoute;
import net.dv8tion.jda.api.sharding.ShardManager;

import static io.javalin.apibuilder.ApiBuilder.path;

public class SettingsRoute implements RoutesBuilder {
    private final AbuseProtectionRoute abuseProtectionRoute = new AbuseProtectionRoute();
    private final IntegrationBypassRoute integrationBypassRoute = new IntegrationBypassRoute();
    private final GeneralRoute generalRoute = new GeneralRoute();
    private final ReputationRoute reputationRoute;
    private final AnnouncementsRoute announcementsRoute = new AnnouncementsRoute();
    private final MessagesRoute messagesRoute = new MessagesRoute();
    private final AutopostRoute autopostRoute;
    private final LogChannelRoute logChannelRoute = new LogChannelRoute();
    private final ProfileRoute profileRoute = new ProfileRoute();
    private final RanksRoute ranksRoute;
    private final AuditLogRoute auditLogRoute;
    private final ThankingRoute thankingRoute = new ThankingRoute();

    public SettingsRoute(
            InteractionHub<?, ?, ?> hub,
            AutopostService autopostService,
            RoleAssigner roleAssigner,
            ShardManager shardManager,
            SettingsAuditLogRepository settingsAuditLogRepository,
            MemberCache memberCache) {
        reputationRoute = new ReputationRoute(hub);
        autopostRoute = new AutopostRoute(autopostService);
        auditLogRoute = new AuditLogRoute(memberCache, settingsAuditLogRepository);
        ranksRoute = new RanksRoute(roleAssigner, shardManager);
    }

    @Override
    public void buildRoutes() {
        path("/settings", () -> {
            abuseProtectionRoute.buildRoutes();
            integrationBypassRoute.buildRoutes();
            generalRoute.buildRoutes();
            reputationRoute.buildRoutes();
            announcementsRoute.buildRoutes();
            messagesRoute.buildRoutes();
            autopostRoute.buildRoutes();
            logChannelRoute.buildRoutes();
            profileRoute.buildRoutes();
            ranksRoute.buildRoutes();
            thankingRoute.buildRoutes();
            auditLogRoute.buildRoutes();
        });
    }
}
