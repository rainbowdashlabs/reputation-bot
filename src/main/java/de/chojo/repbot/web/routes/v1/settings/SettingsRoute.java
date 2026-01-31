package de.chojo.repbot.web.routes.v1.settings;

import de.chojo.repbot.web.routes.RoutesBuilder;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class SettingsRoute implements RoutesBuilder {
    @Override
    public void buildRoutes() {
        path("/settings", () -> {
        });
    }
}
