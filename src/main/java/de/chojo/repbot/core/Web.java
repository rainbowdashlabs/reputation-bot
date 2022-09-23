package de.chojo.repbot.core;

import de.chojo.jdautil.botlist.BotlistService;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.web.Api;
import io.javalin.Javalin;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.ReDocOptions;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.javalin.plugin.openapi.utils.OpenApiVersionUtil;
import io.swagger.v3.oas.models.info.License;
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

        var info = new io.swagger.v3.oas.models.info.Info().version("1.0").title("Reputation Bot API")
                                                           .description("Documentation for the Reputation Bot API")
                                                           .license(new License().name("GNU Affero General Public License v3.0")
                                                                                 .url("https://github.com/RainbowDashLabs/reputation-bot/blob/master/LICENSE.md"));
        var options = new OpenApiOptions(info)
                .path("/json-docs")
                .reDoc(new ReDocOptions("/redoc")) // endpoint for redoc
                .swagger(new SwaggerOptions("/docs").title("Reputation Bot API"));
        OpenApiVersionUtil.INSTANCE.setLogWarnings(false);

        javalin = Javalin.create(config -> config.registerPlugin(new OpenApiPlugin(options)))
                .start(api.host(), api.port());
        new Api(javalin, data.metrics()).init();
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
