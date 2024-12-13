/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.core;

import de.chojo.jdautil.botlist.BotlistService;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.web.Api;
import io.javalin.Javalin;
import io.javalin.openapi.OpenApiLicense;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.OpenApiPluginConfiguration;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Web {
    private static final Logger log = getLogger(Web.class);
    private final Bot bot;
    private final Data data;
    private final Threading threading;
    private final Configuration configuration;
    private Javalin javalin;

    private Web(Bot bot, Data data, Threading threading, Configuration configuration) {
        this.bot = bot;
        this.data = data;
        this.threading = threading;
        this.configuration = configuration;
    }

    public static Web create(Bot bot, Data data, Threading threading, Configuration configuration) {
        var web = new Web(bot, data, threading, configuration);
        web.init();
        return web;
    }

    public void init() {
        initApi();
        initBotList();
    }

    private void initApi() {
        var api = configuration.api();

        /*
        var options = new OpenApiOptions(info)
                .path("/json-docs")
                .reDoc(new ReDocOptions("/redoc")) // endpoint for redoc
                .swagger(new SwaggerOptions("/docs").title("Reputation Bot API"));
        OpenApiVersionUtil.INSTANCE.setLogWarnings(false);*/

        javalin = Javalin.create(config -> config.registerPlugin(new OpenApiPlugin(this::configureOpenApi)))
                .start(api.host(), api.port());
        new Api(javalin, data.metrics()).init();
    }

    private void configureOpenApi(OpenApiPluginConfiguration config) {
        config.withDefinitionConfiguration((version, definition) -> {
            definition.withInfo(info -> {
                info.setTitle("Reputation Bot API");
                info.setVersion("1.0");
                info.setDescription("Documentation for the Reputation Bot API");
                info.setLicense(new OpenApiLicense()
                        .name("GNU Affero General Public License v3.0")
                        .url("https://github.com/RainbowDashLabs/reputation-bot/blob/master/LICENSE.md")
                );
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
        BotlistService.build(bot.shardManager())
                      .forDiscordBotListCOM(botlist.discordBotlistCom())
                      .forDiscordBotsGG(botlist.discordBotsGg())
                      .forTopGG(botlist.topGg())
                      .forBotlistMe(botlist.botListMe())
                      .withExecutorService(threading.repBotWorker())
                      .withVoteService(builder -> builder
                              .withVoteWeebhooks(javalin)
                              .onVote(voteData -> bot.shardManager()
                                                     .retrieveUserById(voteData.userId())
                                                     .flatMap(User::openPrivateChannel)
                                                     .flatMap(channel -> channel.sendMessage("Thanks for voting <3"))
                                                     .queue(message -> log.debug("Vote received"),
                                                             err -> ErrorResponseException.ignore(ErrorResponse.UNKNOWN_USER,
                                                                     ErrorResponse.CANNOT_SEND_TO_USER))
                              )
                              .build()
                      )
                      .build();
    }
}
