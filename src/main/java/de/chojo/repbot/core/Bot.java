/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.core;

import de.chojo.jdautil.interactions.dispatching.InteractionHub;
import de.chojo.jdautil.localization.util.LocaleProvider;
import de.chojo.repbot.actions.messages.log.MessageLog;
import de.chojo.repbot.actions.user.donated.received.UserDonated;
import de.chojo.repbot.actions.user.received.UserReceived;
import de.chojo.repbot.analyzer.ContextResolver;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.commands.abuseprotection.AbuseProtection;
import de.chojo.repbot.commands.bot.BotAdmin;
import de.chojo.repbot.commands.channel.Channel;
import de.chojo.repbot.commands.dashboard.Dashboard;
import de.chojo.repbot.commands.debug.Debug;
import de.chojo.repbot.commands.gdpr.Gdpr;
import de.chojo.repbot.commands.info.Info;
import de.chojo.repbot.commands.invite.Invite;
import de.chojo.repbot.commands.locale.Locale;
import de.chojo.repbot.commands.log.Log;
import de.chojo.repbot.commands.messages.Messages;
import de.chojo.repbot.commands.prune.Prune;
import de.chojo.repbot.commands.reactions.Reactions;
import de.chojo.repbot.commands.repadmin.RepAdmin;
import de.chojo.repbot.commands.repsettings.RepSettings;
import de.chojo.repbot.commands.reputation.Reputation;
import de.chojo.repbot.commands.roles.Roles;
import de.chojo.repbot.commands.scan.Scan;
import de.chojo.repbot.commands.setup.Setup;
import de.chojo.repbot.commands.thankwords.Thankwords;
import de.chojo.repbot.commands.top.Top;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.listener.LogListener;
import de.chojo.repbot.listener.MessageListener;
import de.chojo.repbot.listener.ReactionListener;
import de.chojo.repbot.listener.StateListener;
import de.chojo.repbot.listener.VoiceStateListener;
import de.chojo.repbot.listener.voting.ReputationVoteListener;
import de.chojo.repbot.service.AnalyzerService;
import de.chojo.repbot.service.GdprService;
import de.chojo.repbot.service.MetricService;
import de.chojo.repbot.service.PremiumService;
import de.chojo.repbot.service.PresenceService;
import de.chojo.repbot.service.RepBotCachePolicy;
import de.chojo.repbot.service.RoleAssigner;
import de.chojo.repbot.service.RoleUpdater;
import de.chojo.repbot.service.SelfCleanupService;
import de.chojo.repbot.service.reputation.ReputationService;
import de.chojo.repbot.statistic.Statistic;
import de.chojo.repbot.util.LogNotify;
import de.chojo.repbot.util.PermissionErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.Collections;

import static org.slf4j.LoggerFactory.getLogger;

public class Bot {
    private static final Logger log = getLogger(Bot.class);
    private final Data data;
    private final Threading threading;
    private final Configuration configuration;
    private final Localization localization;
    private ShardManager shardManager;
    private Scan scan;
    private Roles roles;
    private RoleAssigner roleAssigner;
    private RepBotCachePolicy repBotCachePolicy;
    private MessageAnalyzer messageAnalyzer;
    private ReputationService reputationService;
    private ContextResolver contextResolver;
    private Statistic statistic;
    private GdprService gdprService;

    private Bot(Data data, Threading threading, Configuration configuration, Localization localization) {
        this.data = data;
        this.threading = threading;
        this.configuration = configuration;
        this.localization = localization;
    }

    public static Bot create(Data data, Threading threading, Configuration configuration, Localization localizer) throws LoginException {
        var bot = new Bot(data, threading, configuration, localizer);
        bot.init();
        return bot;
    }

    public void init() throws LoginException {
        initShardManager();
        configureRestActions();
        initServices();
        initInteractions();
        initListener();
    }

    private void configureRestActions() {
        log.info("Configuring rest actions.");
        RestAction.setDefaultFailure(throwable -> {
            if (throwable instanceof InsufficientPermissionException perm) {
                PermissionErrorHandler.handle(perm, shardManager, localization.localizer().context(LocaleProvider.empty()), configuration);
                return;
            }
            if (throwable.getCause() instanceof InsufficientPermissionException insuf) {
                PermissionErrorHandler.handle(insuf, shardManager, localization.localizer().context(LocaleProvider.empty()), configuration);
                return;
            }
            if (throwable instanceof ErrorResponseException e) {
                if (e.getErrorResponse() == ErrorResponse.UNKNOWN_INTERACTION) {
                    data.metrics().service().failedInteraction();
                    log.debug("Interaction timed out", e);
                    return;
                }
            }
            log.error(LogNotify.NOTIFY_ADMIN, "Unhandled exception occured: ", throwable);
        });

        RestAction.setDefaultSuccess(suc -> {
            if (suc instanceof InteractionHook) {
                data.metrics().service().successfulInteraction();
            }
        });
    }

    private void initShardManager() throws LoginException {
        log.info("Initializing Shardmanager.");
        roleAssigner = new RoleAssigner(data.guilds(), localization.localizer());
        scan = new Scan(data.guilds(), configuration);
        roles = new Roles(data.guilds(), new RoleAssigner(data.guilds(), localization.localizer()));

        repBotCachePolicy = new RepBotCachePolicy(scan, roles);
        shardManager = DefaultShardManagerBuilder
                .createDefault(configuration.baseSettings().token())
                .enableIntents(
                        // Required to retrieve reputation emotes
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        // Required to scan for thankwords
                        GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT,
                        // Required to resolve member without a direct mention
                        GatewayIntent.GUILD_MEMBERS,
                        // Required to cache voice states for member relationships
                        GatewayIntent.GUILD_VOICE_STATES)
                .enableCache(
                        // Required for voice activity
                        CacheFlag.VOICE_STATE)
                // we have our own shutdown hook
                .setEnableShutdownHook(false)
                .setMemberCachePolicy(repBotCachePolicy)
                .setEventPool(threading.eventThreads())
                .setThreadFactory(Threading.createThreadFactory(threading.jdaGroup()))
                .build();
    }

    private void initServices() {
        log.info("Setting up services");
        var guilds = data.guilds();
        var worker = threading.repBotWorker();

        statistic = Statistic.of(shardManager, data.metrics(), worker);

        contextResolver = new ContextResolver(data.voice(), configuration);
        messageAnalyzer = new MessageAnalyzer(contextResolver, configuration, data.metrics(), guilds);

        PresenceService.start(shardManager, configuration, statistic, worker);
        scan.lateInit(messageAnalyzer);

        // init services
        reputationService = new ReputationService(guilds, contextResolver, roleAssigner, configuration.magicImage(), localization.localizer());
        gdprService = GdprService.of(shardManager, guilds, data.gdpr(), worker);
        SelfCleanupService.create(shardManager, localization.localizer(), guilds, data.cleanup(), configuration, worker);
        AnalyzerService.create(threading.repBotWorker(), data.analyzer());
        MetricService.create(threading.repBotWorker(), data.metrics());
    }

    private void initInteractions() {
        log.info("Setting up interactions");
        var localizer = localization.localizer();
        var guilds = data.guilds();

        InteractionHub.builder(shardManager)
                      .withConversationSystem()
                      .withCommands(
                              new Channel(guilds),
                              new Reputation(guilds, configuration, roleAssigner),
                              roles,
                              new RepSettings(guilds),
                              new Top(guilds),
                              Thankwords.of(messageAnalyzer, guilds),
                              scan,
                              new Locale(guilds),
                              new Invite(configuration),
                              Info.create(configuration),
                              new Log(guilds),
                              Setup.of(guilds, configuration),
                              new Gdpr(data.gdpr()),
                              new Prune(gdprService),
                              new Reactions(guilds),
                              new Dashboard(guilds),
                              new AbuseProtection(guilds),
                              new Debug(guilds),
                              new RepAdmin(guilds, configuration, roleAssigner),
                              new Messages(guilds),
                              new BotAdmin(guilds, configuration, statistic))
                      .withMessages(new MessageLog(guilds))
                      .withUsers(new UserReceived(guilds),
                              new UserDonated(guilds))
                      .withLocalizer(localizer)
                      .cleanGuildCommands("true".equals(System.getProperty("bot.cleancommands", "false")))
                      .testMode("true".equals(System.getProperty("bot.testmode", "false")))
                      .withCommandErrorHandler((context, throwable) -> {
                          if (throwable instanceof InsufficientPermissionException) {
                              PermissionErrorHandler.handle((InsufficientPermissionException) throwable, shardManager,
                                      localizer.context(LocaleProvider.guild(context.guild())), configuration);
                              return;
                          }
                          log.error(LogNotify.NOTIFY_ADMIN, "Command execution of {} failed\n{}",
                                  context.interaction().meta().name(), context.args(), throwable);
                      })
                      .withGuildCommandMapper(cmd -> Collections.singletonList(configuration.baseSettings().botGuild()))
                      .withDefaultMenuService()
                      .withPostCommandHook(result -> data.metrics().commands()
                                                         .logCommand(result.context().interaction().meta().name()))
                      .withPagination(builder -> builder.withLocalizer(localizer).previousText("pages.previous")
                                                        .nextText("pages.next"))
                      .build();
    }

    private void initListener() {
        log.info("Setting up listener.");
        var localizer = localization.localizer();
        var guilds = data.guilds();
        // init listener and services
        var reactionListener = new ReactionListener(guilds, localizer, reputationService, configuration);
        var voteListener = new ReputationVoteListener(guilds, reputationService, localizer, configuration);
        var messageListener = new MessageListener(localizer, configuration, guilds, repBotCachePolicy, voteListener,
                reputationService, contextResolver, messageAnalyzer);
        var voiceStateListener = VoiceStateListener.of(data.voice(), threading.repBotWorker());
        var logListener = LogListener.create(threading.repBotWorker());
        var stateListener = StateListener.of(localizer, guilds, configuration, data.metrics());
        var roleUpdater = RoleUpdater.create(guilds, roleAssigner, shardManager, threading.repBotWorker());
        PremiumService premiumService = PremiumService.of(guilds, threading);

        shardManager.addEventListener(
                reactionListener,
                voteListener,
                messageListener,
                voiceStateListener,
                logListener,
                stateListener,
                roleUpdater,
                premiumService);
    }

    public ShardManager shardManager() {
        return shardManager;
    }

    public void shutdown() {
        shardManager.shutdown();
    }
}
