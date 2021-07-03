package de.chojo.repbot;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.chojo.jdautil.botlist.BotlistReporter;
import de.chojo.jdautil.command.dispatching.CommandHub;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Language;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.analyzer.ContextResolver;
import de.chojo.repbot.analyzer.MessageAnalyzer;
import de.chojo.repbot.commands.Channel;
import de.chojo.repbot.commands.Gdpr;
import de.chojo.repbot.commands.Help;
import de.chojo.repbot.commands.Info;
import de.chojo.repbot.commands.Invite;
import de.chojo.repbot.commands.Locale;
import de.chojo.repbot.commands.Log;
import de.chojo.repbot.commands.Prefix;
import de.chojo.repbot.commands.Prune;
import de.chojo.repbot.commands.Reactions;
import de.chojo.repbot.commands.RepSettings;
import de.chojo.repbot.commands.Reputation;
import de.chojo.repbot.commands.Roles;
import de.chojo.repbot.commands.Scan;
import de.chojo.repbot.commands.Setup;
import de.chojo.repbot.commands.Thankwords;
import de.chojo.repbot.commands.TopReputation;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.data.updater.QueryReplacement;
import de.chojo.repbot.data.updater.SqlUpdater;
import de.chojo.repbot.listener.InternalCommandListener;
import de.chojo.repbot.listener.LogListener;
import de.chojo.repbot.listener.MessageListener;
import de.chojo.repbot.listener.ReactionListener;
import de.chojo.repbot.listener.StateListener;
import de.chojo.repbot.listener.VoiceStateListener;
import de.chojo.repbot.listener.voting.ReputationVoteListener;
import de.chojo.repbot.service.GdprService;
import de.chojo.repbot.service.PresenceService;
import de.chojo.repbot.service.RepBotCachePolicy;
import de.chojo.repbot.service.ReputationService;
import de.chojo.repbot.service.RoleAssigner;
import de.chojo.repbot.statistic.Statistic;
import de.chojo.repbot.util.LogNotify;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.logging.log4j.LogManager;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import static org.slf4j.LoggerFactory.getLogger;

public class ReputationBot {
    private static final Logger log = getLogger(ReputationBot.class);
    private static final Thread.UncaughtExceptionHandler EXCEPTION_HANDLER =
            (t, e) -> log.error(LogNotify.NOTIFY_ADMIN, "An uncaught exception occured in " + t.getName() + "-" + t.getId() + ".", e);
    private static ReputationBot instance;
    private final ThreadGroup eventGroup = new ThreadGroup("Event Worker");
    private final ThreadGroup workerGroup = new ThreadGroup("Scheduled Worker");
    private final ThreadGroup hikariGroup = new ThreadGroup("Hikari Worker");
    private final ThreadGroup jdaGroup = new ThreadGroup("JDA Worker");
    private final ExecutorService eventThreads = Executors.newFixedThreadPool(50, createThreadFactory(eventGroup));
    private final ScheduledExecutorService repBotWorker = Executors.newScheduledThreadPool(3, createThreadFactory(workerGroup));
    private ShardManager shardManager;
    private HikariDataSource dataSource;
    private Configuration configuration;
    private Localizer localizer;
    private Scan scan;
    private RepBotCachePolicy repBotCachePolicy;
    private ContextResolver contextResolver;
    private MessageAnalyzer messageAnalyzer;

    public static void main(String[] args) throws SQLException, IOException {
        ReputationBot.instance = new ReputationBot();
        instance.start();
    }

    private static ThreadFactory createThreadFactory(ThreadGroup group) {
        return r -> {
            var thread = new Thread(group, r, group.getName());
            thread.setUncaughtExceptionHandler(EXCEPTION_HANDLER);
            return thread;
        };
    }

    private void start() throws SQLException, IOException {
        configuration = Configuration.create();

        log.info("Initializing connection pool");

        initDatabase();

        log.info("Creating Shutdown Hook");
        initShutdownHook();

        initLocalization();

        initAnalyzer();

        log.info("Initializing JDA");
        try {
            initJDA();
        } catch (LoginException e) {
            log.error("Could not login.", e);
            return;
        }

        log.info("Initializing bot.");
        initBot();

        initBotList();
    }

    private void initAnalyzer() {
        contextResolver = new ContextResolver(dataSource, configuration);
        messageAnalyzer = new MessageAnalyzer(contextResolver, configuration);
    }

    private void initBotList() {
        var botlist = configuration.botlist();
        if (!botlist.isSubmit()) return;
        BotlistReporter.build(shardManager)
                .forDiscordBotListCOM(botlist.discordBotlistCom())
                .forDiscordBotsGG(botlist.discordBotsGg())
                .forTopGG(botlist.topGg())
                .withExecutorService(repBotWorker)
                .build();
    }

    private void initDatabase() throws SQLException, IOException {
        var connectionPool = getConnectionPool(false);

        var schema = configuration.database().schema();
        SqlUpdater.builder(connectionPool)
                .setReplacements(new QueryReplacement("repbot_schema", schema))
                .setVersionTable(schema + ".repbot_version")
                .setSchemas(schema)
                .execute();
        connectionPool.close();

        dataSource = getConnectionPool(true);
    }

    private void initLocalization() {
        localizer = Localizer.builder(Language.ENGLISH)
                .addLanguage(Language.GERMAN, Language.of("es_ES", "Español"), Language.of("fr_FR", "Français"),
                        Language.of("pt_PT", "Português"))
                .withLanguageProvider(guild -> new GuildData(dataSource).getLanguage(guild))
                .withBundlePath("locale")
                .build();
    }

    private void initBot() {
        var statistic = Statistic.of(shardManager, dataSource, repBotWorker);
        PresenceService.start(shardManager, configuration, statistic, repBotWorker);

        // init services
        var roleAssigner = new RoleAssigner(dataSource);
        var reputationService = new ReputationService(dataSource, contextResolver, roleAssigner, configuration.magicImage(), localizer);
        var gdprService = GdprService.of(shardManager, dataSource, repBotWorker);

        // init listener and services
        var reactionListener = new ReactionListener(dataSource, localizer, reputationService);
        var voteListener = new ReputationVoteListener(reputationService, localizer);
        var messageListener = new MessageListener(dataSource, configuration, repBotCachePolicy, voteListener,
                reputationService, contextResolver, messageAnalyzer, statistic);
        var stateListener = StateListener.of(localizer, dataSource, configuration);
        var voiceStateListener = VoiceStateListener.of(dataSource, repBotWorker);
        var logListener = LogListener.create(repBotWorker);

        shardManager.addEventListener(
                reactionListener,
                voteListener,
                messageListener,
                stateListener,
                voiceStateListener,
                logListener);
        if (configuration.baseSettings().isInternalCommands()) {
            shardManager.addEventListener(new InternalCommandListener(configuration, statistic));
        }

        var data = new GuildData(dataSource);
        var hubBuilder = CommandHub.builder(shardManager, configuration.baseSettings().defaultPrefix())
                .receiveGuildCommands()
                .receiveGuildMessagesUpdates()
                .withConversationSystem()
                .withPrefixResolver(data::getPrefix)
                .withSlashCommands()
                .withCommands(
                        new Channel(dataSource, localizer),
                        new Prefix(dataSource, configuration, localizer),
                        new Reputation(dataSource, localizer, configuration),
                        new Roles(dataSource, localizer),
                        new RepSettings(dataSource, localizer),
                        new TopReputation(dataSource, localizer),
                        Thankwords.of(messageAnalyzer, dataSource, localizer),
                        scan,
                        new Locale(dataSource, localizer),
                        new Invite(localizer, configuration),
                        Info.create(localizer, configuration),
                        new Log(shardManager, dataSource, localizer),
                        Setup.of(dataSource, localizer),
                        new Gdpr(dataSource, localizer),
                        new Prune(gdprService, localizer),
                        new Reactions(dataSource, localizer)
                )
                .withInvalidArgumentProvider(((loc, command) -> Help.getCommandHelp(command, loc)))
                .withLocalizer(localizer)
                .withPermissionCheck((wrapper, command) -> {
                    if (wrapper.getMember().hasPermission(command.permission())) return true;
                    var guildSettings = data.getGuildSettings(wrapper.getGuild());
                    if (guildSettings.isEmpty()) return false;
                    var settings = guildSettings.get();
                    var roleById = wrapper.getGuild().getRoleById(settings.managerRole().orElse(0));
                    if (roleById == null) return false;
                    return wrapper.getMember().getRoles().contains(roleById);
                })
                .withCommandErrorHandler((context, throwable) -> {
                    if (throwable instanceof InsufficientPermissionException) {
                        var permissionException = (InsufficientPermissionException) throwable;
                        var permission = permissionException.getPermission();
                        var errorMessage = localizer.localize("error.missingPermission", context.guild(),
                                Replacement.create("PERM", permission.getName(), Format.BOLD));
                        if (context.guild().getSelfMember().hasPermission(permission)) {
                            errorMessage += "\n" + localizer.localize("error.missingPermissionChannel", context.guild(),
                                    Replacement.createMention((TextChannel) context.channel()));
                        } else {
                            errorMessage += "\n" + localizer.localize("error.missingPermissionGuild", context.guild());
                        }
                        if (permissionException.getPermission() != Permission.MESSAGE_WRITE) {
                            context.channel().sendMessage(errorMessage).queue();
                            return;
                        }
                        // botlists always have permission issues. We will ignore them and wont try to notify anyone...
                        if (configuration.botlist().isBotlistGuild(permissionException.getGuildId())) return;
                        var ownerId = context.guild().getOwnerIdLong();
                        var finalErrorMessage = errorMessage;
                        context.guild().retrieveMemberById(ownerId)
                                .flatMap(member -> member.getUser().openPrivateChannel())
                                .flatMap(privateChannel -> privateChannel.sendMessage(finalErrorMessage))
                                .onErrorMap(t -> null)
                                .queue();
                        return;
                    }
                    log.error(LogNotify.NOTIFY_ADMIN, "Command execution of {} failed\n{}", context.command().command(), context.args(), throwable);
                });
        if (configuration.testMode().isTestMode()) {
            hubBuilder.onlyGuildCommands(configuration.testMode().testGuilds());
        }
        var hub = hubBuilder.build();
        hub.registerCommands(new Help(hub, localizer, configuration.baseSettings().isExclusiveHelp()));
    }

    private void initShutdownHook() {
        var shutdown = new Thread(() -> {
            log.info("Shuting down shardmanager.");
            shardManager.shutdown();
            log.info("Shutting down scheduler.");
            repBotWorker.shutdown();
            log.info("Shutting down database connections.");
            dataSource.close();
            log.info("Bot shutdown complete.");
            LogManager.shutdown();
        });
        Runtime.getRuntime().addShutdownHook(shutdown);
    }

    private void initJDA() throws LoginException {
        scan = new Scan(dataSource, messageAnalyzer, localizer);
        repBotCachePolicy = new RepBotCachePolicy(scan);
        shardManager = DefaultShardManagerBuilder.createDefault(configuration.baseSettings().token())
                .enableIntents(
                        // Required to retrieve reputation emotes
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        // Required to scan for thankwords
                        GatewayIntent.GUILD_MESSAGES,
                        // Required to resolve member without a direct mention
                        GatewayIntent.GUILD_MEMBERS,
                        // For online status caching
                        GatewayIntent.GUILD_PRESENCES)
                .enableCache(
                        // Required for voice activity
                        CacheFlag.VOICE_STATE,
                        //Required for custom member cache
                        CacheFlag.ONLINE_STATUS)
                // we have our own shutdown hook
                .setEnableShutdownHook(false)
                .setMemberCachePolicy(repBotCachePolicy)
                .setEventPool(eventThreads)
                .setThreadFactory(createThreadFactory(jdaGroup))
                .build();
    }

    private HikariDataSource getConnectionPool(boolean withSchema) {
        var db = configuration.database();
        var props = new Properties();
        props.setProperty("dataSourceClassName", PGSimpleDataSource.class.getName());
        props.setProperty("dataSource.serverName", db.host());
        props.setProperty("dataSource.portNumber", db.port());
        props.setProperty("dataSource.user", db.user());
        props.setProperty("dataSource.password", db.password());
        props.setProperty("dataSource.databaseName", db.database());

        var config = new HikariConfig(props);
        config.setMaximumPoolSize(db.poolSize());
        if (withSchema) {
            config.setSchema(db.schema());
        }
        config.setThreadFactory(createThreadFactory(hikariGroup));

        return new HikariDataSource(config);
    }
}
