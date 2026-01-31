package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.repbot.dao.access.guild.settings.sub.AbuseProtection;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.settings.sub.AbuseProtectionPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.sessions.GuildSession;
import io.javalin.http.Context;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class AbuseProtectionRoute implements RoutesBuilder {
    public void updateAbuseSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        AbuseProtection abuseProtection = session.repGuild().settings().abuseProtection();
        AbuseProtectionPOJO abuseProtectionPOJO = ctx.bodyAsClass(AbuseProtectionPOJO.class);
        abuseProtection.apply(abuseProtectionPOJO);
    }

    @Override
    public void buildRoutes() {
        path("/abuseprotection", () -> {
            post("", this::updateAbuseSettings, Role.GUILD_USER);
        });
    }
}
