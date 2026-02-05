/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.core;

import de.chojo.jdautil.interactions.dispatching.InteractionHub;
import de.chojo.jdautil.interactions.message.Message;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.user.User;
import de.chojo.jdautil.localization.util.LocaleProvider;
import de.chojo.jdautil.util.SysVar;
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
import de.chojo.repbot.commands.profile.Profile;
import de.chojo.repbot.commands.prune.Prune;
import de.chojo.repbot.commands.ranking.Ranking;
import de.chojo.repbot.commands.reactions.Reactions;
import de.chojo.repbot.commands.repadmin.RepAdmin;
import de.chojo.repbot.commands.repsettings.RepSettings;
import de.chojo.repbot.commands.reputation.Reputation;
import de.chojo.repbot.commands.roles.Roles;
import de.chojo.repbot.commands.scan.Scan;
import de.chojo.repbot.commands.settings.Settings;
import de.chojo.repbot.commands.setup.Setup;
import de.chojo.repbot.commands.supporter.Supporter;
import de.chojo.repbot.commands.thankwords.Thankwords;
import de.chojo.repbot.commands.top.Top;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.exceptions.MissingSupportTier;
import de.chojo.repbot.listener.LogListener;
import de.chojo.repbot.listener.MessageListener;
import de.chojo.repbot.listener.ReactionListener;
import de.chojo.repbot.listener.StateListener;
import de.chojo.repbot.listener.VoiceStateListener;
import de.chojo.repbot.listener.voting.ReputationVoteListener;
import de.chojo.repbot.service.AnalyzerService;
import de.chojo.repbot.service.AutopostService;
import de.chojo.repbot.service.ChatSupportService;
import de.chojo.repbot.service.GdprService;
import de.chojo.repbot.service.MetricService;
import de.chojo.repbot.service.MonitorService;
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
import de.chojo.repbot.web.sessions.SessionService;
import net.dv8tion.jda.api.entities.ISnowflake;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import javax.security.auth.login.LoginException;

import static de.chojo.repbot.util.States.TEST_MODE;
import static org.slf4j.LoggerFactory.getLogger;

public class Bot {
    private static final Logger log = getLogger(Bot.class);
    private static final Set<ErrorResponse> IGNORE_ERRORS =
            Set.of(ErrorResponse.ILLEGAL_OPERATION_ARCHIVED_THREAD, ErrorResponse.MISSING_ACCESS);
    // TODO: Remove after decomissioning the commands
    public static String WEB_COMMAND_MENTION = "";
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
    private AutopostService autopostService;
    private PremiumService premiumService;
    private InteractionHub<Slash, Message, User> hub;
    private SessionService sessionService;

    private Bot(Data data, Threading threading, Configuration configuration, Localization localization) {
        this.data = data;
        this.threading = threading;
        this.configuration = configuration;
        this.localization = localization;
    }

    public static Bot create(Data data, Threading threading, Configuration configuration, Localization localizer)
            throws LoginException {
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

    public ShardManager shardManager() {
        return shardManager;
    }

    public void shutdown() {
        shardManager.shutdown();
    }

    public InteractionHub<Slash, Message, User> hub() {
        return hub;
    }

    public Localization localization() {
        return localization;
    }

    public AutopostService autopostService() {
        return autopostService;
    }

    public RoleAssigner roleAssigner() {
        return roleAssigner;
    }

    public SessionService sessionService() {
        return sessionService;
    }

    private void initShardManager() throws LoginException {
        log.info("Initializing Shardmanager.");
        roleAssigner = new RoleAssigner(data.guildRepository(), localization.localizer());
        scan = new Scan(data.guildRepository(), configuration);
        roles = new Roles(data.guildRepository(), new RoleAssigner(data.guildRepository(), localization.localizer()));

        repBotCachePolicy = new RepBotCachePolicy(scan, roles);
        shardManager = DefaultShardManagerBuilder.createDefault(SysVar.envOrProp(
                        "BOT_TOKEN", "bot.token", configuration.baseSettings().token()))
                .setEventPassthrough(true)
                .enableIntents(
                        // Required to retrieve reputation emotes
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        // Required to scan for thankwords
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
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

    private void configureRestActions() {
        log.info("Configuring rest actions.");
        RestAction.setDefaultFailure(throwable -> {
            if (throwable instanceof InsufficientPermissionException perm) {
                PermissionErrorHandler.handle(
                        perm, shardManager, localization.localizer().context(LocaleProvider.empty()), configuration);
                return;
            }
            if (throwable.getCause() instanceof InsufficientPermissionException perm) {
                PermissionErrorHandler.handle(
                        perm, shardManager, localization.localizer().context(LocaleProvider.empty()), configuration);
                return;
            }
            if (throwable instanceof ErrorResponseException e) {
                if (e.getErrorResponse() == ErrorResponse.UNKNOWN_INTERACTION) {
                    data.metrics().service().failedInteraction();
                    log.debug("Interaction timed out", e);
                    return;
                }
                if (e.getErrorResponse() == ErrorResponse.MISSING_PERMISSIONS) {
                    data.metrics().service().failedInteraction();
                    log.debug("Interaction timed out", e);
                    return;
                }
                if (IGNORE_ERRORS.contains(e.getErrorResponse())) {
                    log.debug("Ignored error response", e);
                    return;
                }
            }
            log.error(LogNotify.NOTIFY_ADMIN, "Unhandled exception occurred: ", throwable);
        });

        RestAction.setDefaultSuccess(suc -> {
            if (suc instanceof InteractionHook) {
                data.metrics().service().successfulInteraction();
            }
        });
    }

    private void initServices() {
        log.info("Setting up services");
        var guilds = data.guildRepository();
        var worker = threading.repBotWorker();
        sessionService = new SessionService(configuration, data.guildSessionRepository(), data.guildRepository(), shardManager);


        statistic = Statistic.of(shardManager, data.metrics(), worker);

        contextResolver = new ContextResolver(data.voice(), configuration);
        messageAnalyzer = new MessageAnalyzer(contextResolver, configuration, data.metrics(), guilds);

        PresenceService.start(shardManager, configuration, statistic, worker);
        scan.lateInit(messageAnalyzer);

        // init services
        reputationService =
                new ReputationService(guilds, contextResolver, roleAssigner, configuration, localization.localizer());
        gdprService = GdprService.of(shardManager, guilds, data.gdpr(), worker);
        SelfCleanupService.create(
                shardManager, localization.localizer(), guilds, data.cleanup(), configuration, worker);
        AnalyzerService.create(threading.repBotWorker(), data.analyzer());
        MetricService.create(threading.repBotWorker(), data.metrics());
        autopostService =
                AutopostService.create(shardManager, data.guildRepository(), threading, localization.localizer());
        premiumService = PremiumService.of(guilds, threading, configuration, localization.localizer(), shardManager);
    }

    private void initInteractions() {
        log.info("Setting up interactions");
        var localizer = localization.localizer();
        var guilds = data.guildRepository();

        BotAdmin botAdmin = new BotAdmin(guilds, configuration, statistic, sessionService());
        hub = InteractionHub.builder(shardManager)
                .withConversationSystem()
                .withCommands(
                        new Channel(guilds, configuration, autopostService),
                        new Profile(guilds, configuration, roleAssigner),
                        roles,
                        new RepSettings(guilds, configuration),
                        new Top(guilds, configuration),
                        Thankwords.of(messageAnalyzer, guilds),
                        scan,
                        new Locale(guilds),
                        new Invite(configuration),
                        Info.create(configuration),
                        new Log(guilds, configuration),
                        new Setup(sessionService()),
                        new Gdpr(data.gdpr()),
                        new Prune(gdprService),
                        new Reactions(guilds, configuration),
                        new Dashboard(guilds),
                        new AbuseProtection(guilds),
                        new Debug(guilds),
                        new RepAdmin(guilds, configuration, roleAssigner, premiumService),
                        new Messages(guilds),
                        botAdmin,
                        new Ranking(guilds, configuration),
                        new Reputation(guilds, reputationService) /*TODO: remove rep command*/,
                        new Supporter(premiumService, configuration, guilds),
                        new Settings(sessionService()))
                .withMessages(new MessageLog(guilds))
                .withUsers(new UserReceived(guilds, configuration), new UserDonated(guilds, configuration))
                .withLocalizer(localizer)
                .cleanGuildCommands("true".equals(System.getProperty("bot.cleancommands", "false")))
                .withCommandErrorHandler((context, throwable) -> {
                    if (throwable instanceof InsufficientPermissionException perm) {
                        PermissionErrorHandler.handle(
                                perm,
                                shardManager,
                                localizer.context(LocaleProvider.guild(context.guild())),
                                configuration);
                        return;
                    }

                    if (throwable instanceof MissingSupportTier ex) {
                        premiumService.handleMissingSupportTier(context, ex);
                        return;
                    }

                    log.error(
                            LogNotify.NOTIFY_ADMIN,
                            "Command execution of {} failed\n{}",
                            context.interaction().meta().name(),
                            context.args(),
                            throwable);
                })
                .withGuildCommandMapper(cmd -> switch (cmd.name()) {
                    case "bot" ->
                        Collections.singletonList(configuration.baseSettings().botGuild());
                    case "reputation" -> data.guildRepository().byCommandReputationEnabled();
                    default -> Collections.emptyList();
                })
                .withDefaultMenuService()
                .withPostCommandHook(result -> data.metrics()
                        .commands()
                        .logCommand(result.context().interaction().meta().name()))
                .withPagination(builder -> builder.withLocalizer(localizer)
                        .previousText("pages.previous")
                        .nextText("pages.next"))
                .withEntitlementProvider((user, guild) -> {
                    // currently no user skus exist that we are interested in.
                    if (guild != null)
                        return new ArrayList<>(
                                guilds.guild(guild).subscriptions().sku());
                    return Collections.emptyList();
                })
                .build();

        // TODO: Remove after decomissioning the settings commands
        Long settingsCommandId;
        if (TEST_MODE) {
            settingsCommandId = shardManager
                    .getShards()
                    .getFirst()
                    .getGuildById(configuration.baseSettings().botGuild())
                    .retrieveCommands()
                    .complete()
                    .stream()
                    .filter(cmd -> cmd.getName().equals("settings"))
                    .map(ISnowflake::getIdLong)
                    .findFirst()
                    .get();

        } else {
            settingsCommandId = shardManager.getShards().getFirst().retrieveCommands().complete().stream()
                    .filter(cmd -> cmd.getName().equals("settings"))
                    .map(ISnowflake::getIdLong)
                    .findFirst()
                    .get();
        }
        WEB_COMMAND_MENTION = "</settings:" + settingsCommandId + ">";
    }

    private void initListener() {
        log.info("Setting up listener.");
        var localizer = localization.localizer();
        var guilds = data.guildRepository();
        // init listener and services
        var reactionListener = new ReactionListener(guilds, localizer, reputationService, configuration);
        var voteListener = new ReputationVoteListener(guilds, reputationService, localizer, configuration);
        var messageListener = new MessageListener(
                localizer,
                configuration,
                guilds,
                repBotCachePolicy,
                voteListener,
                reputationService,
                contextResolver,
                messageAnalyzer);
        var voiceStateListener = VoiceStateListener.of(data.voice(), threading.repBotWorker());
        var logListener = LogListener.create(threading.repBotWorker());
        var stateListener = StateListener.of(localizer, guilds, configuration);
        var roleUpdater = RoleUpdater.create(guilds, roleAssigner, shardManager, threading.repBotWorker());
        ChatSupportService chatSupportService =
                new ChatSupportService(configuration, shardManager, hub.pageServices(), guilds, hub.buttonService());

        shardManager.addEventListener(
                reactionListener,
                voteListener,
                messageListener,
                voiceStateListener,
                logListener,
                stateListener,
                roleUpdater,
                premiumService,
                chatSupportService,
                new MonitorService(data));
    }
}
