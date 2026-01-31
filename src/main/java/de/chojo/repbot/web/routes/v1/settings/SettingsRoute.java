package de.chojo.repbot.web.routes.v1.settings;

import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.routes.v1.settings.sub.AbuseProtectionRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.AnnouncementsRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.AutopostRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.GeneralRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.LogChannelRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.MessagesRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.ReputationRoute;
import de.chojo.repbot.web.routes.v1.settings.sub.ThankingRoute;

import static io.javalin.apibuilder.ApiBuilder.path;

public class SettingsRoute implements RoutesBuilder {
    private final AbuseProtectionRoute abuseProtectionRoute = new AbuseProtectionRoute();
    private final GeneralRoute generalRoute = new GeneralRoute();
    private final ReputationRoute reputationRoute = new ReputationRoute();
    private final AnnouncementsRoute announcementsRoute = new AnnouncementsRoute();
    private final MessagesRoute messagesRoute = new MessagesRoute();
    private final AutopostRoute autopostRoute = new AutopostRoute();
    private final LogChannelRoute logChannelRoute = new LogChannelRoute();
    private final ThankingRoute thankingRoute = new ThankingRoute();

    @Override
    public void buildRoutes() {
        path("/settings", () -> {
            abuseProtectionRoute.buildRoutes();
            generalRoute.buildRoutes();
            reputationRoute.buildRoutes();
            announcementsRoute.buildRoutes();
            messagesRoute.buildRoutes();
            autopostRoute.buildRoutes();
            logChannelRoute.buildRoutes();
            thankingRoute.buildRoutes();
        });
    }
}
