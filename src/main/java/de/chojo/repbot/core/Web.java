/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.exc.InputCoercionException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.chojo.jdautil.botlist.BotlistService;
import de.chojo.jdautil.interactions.dispatching.InteractionHub;
import de.chojo.logutil.marker.LogNotify;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.service.AutopostService;
import de.chojo.repbot.web.Api;
import de.chojo.repbot.web.cache.MemberCache;
import de.chojo.repbot.web.config.Role;
import de.chojo.repbot.web.config.SessionAttribute;
import de.chojo.repbot.web.error.ApiException;
import de.chojo.repbot.web.error.ErrorResponseWrapper;
import de.chojo.repbot.web.error.PremiumFeatureException;
import de.chojo.repbot.web.sessions.SessionService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.json.JavalinJackson;
import io.javalin.openapi.OpenApiLicense;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.OpenApiPluginConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import io.javalin.security.RouteRole;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

public class Web {
    private static final Logger log = getLogger(Web.class);
    private final Bot bot;
    private final Data data;
    private final Threading threading;
    private final Configuration configuration;
    private final SessionService sessionService;
    private final InteractionHub<?, ?, ?> interactionHub;
    private final AutopostService autopostService;
    private final MemberCache memberCache = new MemberCache();
    private Javalin javalin;

    private Web(
            Bot bot,
            Data data,
            Threading threading,
            Configuration configuration,
            SessionService sessionService,
            InteractionHub<?, ?, ?> interactionHub,
            AutopostService autopostService) {
        this.bot = bot;
        this.data = data;
        this.threading = threading;
        this.configuration = configuration;
        this.sessionService = sessionService;
        this.interactionHub = interactionHub;
        this.autopostService = autopostService;
    }

    public static Web create(
            Bot bot,
            Data data,
            Threading threading,
            Configuration configuration,
            SessionService sessionService,
            InteractionHub<?, ?, ?> interactionHub,
            AutopostService autopostService) {
        var web = new Web(bot, data, threading, configuration, sessionService, interactionHub, autopostService);
        web.init();
        return web;
    }

    public static JavalinJackson jacksonMapper() {
        SimpleModule longAsStringModule = new SimpleModule();
        longAsStringModule.addSerializer(Long.class, ToStringSerializer.instance);
        longAsStringModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .addModule(longAsStringModule)
                .build();

        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
        return new JavalinJackson(mapper, true);
    }

    public void init() {
        initApi();
        initBotList();
    }

    public void handleAccess(Context ctx) {
        Set<RouteRole> routeRoles = ctx.routeRoles();
        if (routeRoles.contains(Role.ANYONE)) {
            return;
        }

        if (routeRoles.contains(Role.GUILD_USER)) {
            var session = sessionService.getGuildSession(ctx).orElseThrow(() -> {
                ctx.header("WWW-Authenticate", "Authorization");
                return new UnauthorizedResponse("You need to be logged in to access this route.");
            });
            session.validate();
            ctx.sessionAttribute(SessionAttribute.GUILD_SESSION, session);
        }
    }

    private void initApi() {
        var api = configuration.api();

        /*
        var options = new OpenApiOptions(info)
                .path("/json-docs")
                .reDoc(new ReDocOptions("/redoc")) // endpoint for redoc
                .swagger(new SwaggerOptions("/docs").title("Reputation Bot API"));
        OpenApiVersionUtil.INSTANCE.setLogWarnings(false);*/

        javalin = Javalin.create(config -> {
                    config.registerPlugin(new OpenApiPlugin(this::configureOpenApi));
                    config.registerPlugin(new SwaggerPlugin(this::configureSwagger));
                    config.bundledPlugins.enableCors(cors -> {
                        cors.addRule(CorsPluginConfig.CorsRule::anyHost);
                    });
                    // Serve static files from external "public" directory when available; fall back to classpath
                    var publicDir = Path.of("public");
                    if (Files.isDirectory(publicDir)) {
                        config.staticFiles.add(publicDir.toString(), io.javalin.http.staticfiles.Location.EXTERNAL);
                    } else {
                        config.staticFiles.add("/static", io.javalin.http.staticfiles.Location.CLASSPATH);
                    }
                    config.router.apiBuilder(() -> new Api(
                                    sessionService,
                                    data.metrics(),
                                    bot.hub(),
                                    bot.localization(),
                                    autopostService,
                                    bot.roleAssigner(),
                                    bot.shardManager(),
                                    configuration,
                                    data.settingsAuditLogRepository(),
                                    memberCache,
                                    data.guildRepository())
                            .init());
                    config.router.mount(router -> {
                        router.beforeMatched(this::handleAccess);
                    });
                    config.jsonMapper(jacksonMapper());
                    // Serve frontend SPA
                    if (Files.isDirectory(publicDir)) {
                        config.spaRoot.addFile(
                                "/",
                                publicDir.resolve("index.html").toString(),
                                io.javalin.http.staticfiles.Location.EXTERNAL);
                    } else {
                        config.spaRoot.addFile(
                                "/", "/static/index.html", io.javalin.http.staticfiles.Location.CLASSPATH);
                    }
                })
                .start(api.host(), api.port());
        // Handle specific PremiumFeatureException with detailed JSON
        javalin.exception(PremiumFeatureException.class, (err, ctx) -> {
            var response = new ErrorResponseWrapper("Supporter Required", err.getMessage(), err.details());
            ctx.json(response).status(err.status());
        });

        javalin.exception(JsonMappingException.class, (err, ctx) -> {
            log.error(LogNotify.NOTIFY_ADMIN, "Invalid JSON on route {}", ctx.path(), err);
            if (err.getCause() instanceof InputCoercionException input) {
                ctx.json(new ErrorResponseWrapper(
                                "Invalid Input",
                                input.getMessage().lines().findFirst().get()))
                        .status(HttpStatus.BAD_REQUEST);
                return;
            }
            ctx.json(new ErrorResponseWrapper(
                            "Invalid Input",
                            err.getMessage().lines().findFirst().get()))
                    .status(HttpStatus.BAD_REQUEST);
        });

        javalin.exception(InputCoercionException.class, (err, ctx) -> {
            ctx.json(new ErrorResponseWrapper(
                            "Invalid Input: %s (%s)".formatted(err.getInputType(), err.getMessage()), err.getMessage()))
                    .status(HttpStatus.BAD_REQUEST);
        });

        // Handle generic ApiException with simple JSON
        javalin.exception(ApiException.class, (err, ctx) -> {
            var response = new ErrorResponseWrapper(err.getClass().getSimpleName(), err.getMessage());
            ctx.json(response).status(err.status());
        });

        javalin.exception(Exception.class, (err, ctx) -> {
            log.error(LogNotify.NOTIFY_ADMIN, "Unhandled exception on route {}", ctx.path(), err);
        });
    }

    private void configureSwagger(SwaggerConfiguration swaggerConfiguration) {
        swaggerConfiguration.setDocumentationPath("/docs");
        swaggerConfiguration.setUiPath("/swagger-ui");
    }

    private void configureOpenApi(OpenApiPluginConfiguration config) {
        config.withDocumentationPath("/docs").withDefinitionConfiguration((version, definition) -> {
            definition.withInfo(info -> {
                info.setTitle("Reputation Bot API");
                info.setVersion("1.0");
                info.setDescription("Documentation for the Reputation Bot API");
                info.setLicense(new OpenApiLicense()
                        .name("GNU Affero General Public License v3.0")
                        .url("https://github.com/RainbowDashLabs/reputation-bot/blob/master/LICENSE.md"));
            });
            definition.withServer(openApiServer -> {
                openApiServer.setUrl("https://repbot.rainbowdashlabs.de");
                openApiServer.setDescription("Main server");
            });
        });
    }

    private void initBotList() {
        var botlist = configuration.botlist();
        if (!botlist.isSubmit()) return;
        BotlistService.build(bot.shardManager(), () -> bot.shardManager().getGuilds().size(), () ->  bot.shardManager().getUsers().size())
                .forDiscordBotListCOM(botlist.discordBotlistCom())
                .forDiscordBotsGG(botlist.discordBotsGg())
                .forTopGG(botlist.topGg())
                .forBotlistMe(botlist.botListMe())
                .withExecutorService(threading.repBotWorker())
                .withVoteService(builder -> builder.withVoteWeebhooks(javalin)
                        .onVote(voteData -> bot.shardManager()
                                .retrieveUserById(voteData.userId())
                                .flatMap(User::openPrivateChannel)
                                .flatMap(channel -> channel.sendMessage("Thanks for voting <3"))
                                .queue(
                                        message -> log.debug("Vote received"),
                                        err -> ErrorResponseException.ignore(
                                                ErrorResponse.UNKNOWN_USER, ErrorResponse.CANNOT_SEND_TO_USER)))
                        .build())
                .build();
    }
}
