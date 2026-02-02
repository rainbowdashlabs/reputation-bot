package de.chojo.repbot.web.routes.v1.settings.sub;

import de.chojo.jdautil.interactions.dispatching.InteractionHub;
import de.chojo.repbot.dao.access.guild.settings.sub.Reputation;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.pojo.settings.sub.ReputationPOJO;
import de.chojo.repbot.web.routes.RoutesBuilder;
import de.chojo.repbot.web.sessions.GuildSession;
import io.javalin.http.Context;
import io.javalin.http.FailedDependencyResponse;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static org.slf4j.LoggerFactory.getLogger;

public class ReputationRoute implements RoutesBuilder {
    private static final Logger log = getLogger(ReputationRoute.class);
    private final InteractionHub<?, ?, ?> hub;

    public ReputationRoute(InteractionHub<?, ?, ?> hub) {
        this.hub = hub;
    }

    @OpenApi(
            summary = "Update reputation settings",
            operationId = "updateReputationSettings",
            path = "v1/settings/reputation",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @io.javalin.openapi.OpenApiContent(from = ReputationPOJO.class)),
            responses = {@OpenApiResponse(status = "200"), @OpenApiResponse(status = "424", description = "Could not refresh guild commands.")}
    )
    public void updateReputationSettings(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        Reputation reputation = session.repGuild().settings().reputation();
        ReputationPOJO reputationPOJO = ctx.bodyAsClass(ReputationPOJO.class);
        var redeploy = reputation.isCommandActive() != reputationPOJO.isCommandActive();
        reputation.apply(reputationPOJO);
        if (redeploy) {
            refreshGuildCommands(session.repGuild().guild());
        }
    }

    @OpenApi(
            summary = "Update reputation reaction active",
            operationId = "updateReputationReactionActive",
            path = "v1/settings/reputation/reactionactive",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateReactionActive(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().reputation().reactionActive(ctx.bodyAsClass(Boolean.class));
    }

    @OpenApi(
            summary = "Update reputation answer active",
            operationId = "updateReputationAnswerActive",
            path = "v1/settings/reputation/answeractive",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateAnswerActive(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().reputation().answerActive(ctx.bodyAsClass(Boolean.class));
    }

    @OpenApi(
            summary = "Update reputation mention active",
            operationId = "updateReputationMentionActive",
            path = "v1/settings/reputation/mentionactive",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateMentionActive(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().reputation().mentionActive(ctx.bodyAsClass(Boolean.class));
    }

    @OpenApi(
            summary = "Update reputation fuzzy active",
            operationId = "updateReputationFuzzyActive",
            path = "v1/settings/reputation/fuzzyactive",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateFuzzyActive(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().reputation().fuzzyActive(ctx.bodyAsClass(Boolean.class));
    }

    @OpenApi(
            summary = "Update reputation embed active",
            operationId = "updateReputationEmbedActive",
            path = "v1/settings/reputation/embedactive",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateEmbedActive(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().reputation().embedActive(ctx.bodyAsClass(Boolean.class));
    }

    @OpenApi(
            summary = "Update reputation direct active",
            operationId = "updateReputationDirectActive",
            path = "v1/settings/reputation/directactive",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateDirectActive(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().reputation().directActive(ctx.bodyAsClass(Boolean.class));
    }

    @OpenApi(
            summary = "Update reputation command active",
            operationId = "updateReputationCommandActive",
            path = "v1/settings/reputation/commandactive",
            methods = HttpMethod.POST,
            headers = {@OpenApiParam(name = "Authorization", required = true, description = "Guild Session Token")},
            tags = {"Settings"},
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Boolean.class)),
            responses = {@OpenApiResponse(status = "200"), @OpenApiResponse(status = "424", description = "Could not refresh guild commands.")}
    )
    public void updateCommandActive(Context ctx) {
        GuildSession session = ctx.sessionAttribute(SessionAttribute.GUILD_SESSION);
        session.repGuild().settings().reputation().commandActive(ctx.bodyAsClass(Boolean.class));
        refreshGuildCommands(session.repGuild().guild());
    }

    private boolean refreshGuildCommands(Guild guild) {
        // The command needs to be hidden or enabled additionally
        try {
            hub.refreshGuildCommands(guild);
            return true;
        } catch (Exception err) {
            log.error("Error during command refresh", err);
            throw new FailedDependencyResponse("Could not refresh guild commands.");
        }
    }

    @Override
    public void buildRoutes() {
        path("reputation", () -> {
            post("", this::updateReputationSettings, Role.GUILD_USER);
            post("reactionactive", this::updateReactionActive, Role.GUILD_USER);
            post("answeractive", this::updateAnswerActive, Role.GUILD_USER);
            post("mentionactive", this::updateMentionActive, Role.GUILD_USER);
            post("fuzzyactive", this::updateFuzzyActive, Role.GUILD_USER);
            post("embedactive", this::updateEmbedActive, Role.GUILD_USER);
            post("directactive", this::updateDirectActive, Role.GUILD_USER);
            post("commandactive", this::updateCommandActive, Role.GUILD_USER);
        });
    }
}
