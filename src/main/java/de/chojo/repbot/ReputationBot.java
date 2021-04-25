package de.chojo.repbot;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.chojo.jdautil.listener.CommandHub;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Language;
import de.chojo.repbot.commands.Channel;
import de.chojo.repbot.commands.Help;
import de.chojo.repbot.commands.Prefix;
import de.chojo.repbot.commands.RepSettings;
import de.chojo.repbot.commands.Reputation;
import de.chojo.repbot.commands.Roles;
import de.chojo.repbot.commands.Scan;
import de.chojo.repbot.commands.Thankwords;
import de.chojo.repbot.config.ConfigFile;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.listener.MessageListener;
import de.chojo.repbot.listener.ReactionListener;
import de.chojo.repbot.listener.StateListener;
import de.chojo.repbot.manager.RoleAssigner;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.postgresql.ds.PGSimpleDataSource;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class ReputationBot {
    private static ReputationBot instance;
    private final ExecutorService executorService = Executors.newFixedThreadPool(50);
    private ShardManager shardManager;
    private HikariDataSource dataSource;
    private Configuration configuration;
    private Localizer localizer;

    public static void main(String[] args) throws SQLException, IOException {
        ReputationBot.instance = new ReputationBot();
        instance.start();
    }

    private void start() throws SQLException, IOException {
        configuration = Configuration.create();
        log.info("Initializing JDA");
        try {
            initJDA();
        } catch (LoginException e) {
            log.error("Could not login.", e);
            return;
        }

        log.info("Initializing connection pool");
        initConnectionPool();

        initDatabase();

        log.info("Creating Shutdown Hook");
        initShutdownHook();

        initLocalization();

        log.info("Initializing bot.");
        initBot();
    }

    private void initDatabase() throws SQLException, IOException {
        try (var in = getClass().getClassLoader().getResourceAsStream("dbsetup.sql")) {
            var upgrade = new String(in.readAllBytes());
            try (var conn = dataSource.getConnection(); var stmt = conn.prepareStatement(upgrade)){
                stmt.execute();
            }
        } catch (IOException e) {
            log.info("Could not read upgrade script.", e);
            throw e;
        }
        log.info("Database update done.");
    }

    private void initLocalization() {
        localizer = Localizer.builder(Language.ENGLISH)
                .addLanguage(Language.GERMAN)
                .withLanguageProvider(guild -> new GuildData(dataSource).getLanguage(guild))
                .withBundlePath("locale")
                .build();
    }

    private void initBot() {
        var roleAssigner = new RoleAssigner(dataSource);

        shardManager.addEventListener(
                new MessageListener(dataSource, configuration, roleAssigner),
                new StateListener(dataSource),
                new ReactionListener(dataSource, roleAssigner));

        var hub = CommandHub.builder(shardManager, configuration.get().getDefaultPrefix())
                .receiveGuildMessage()
                .receiveGuildMessagesUpdates()
                .withConversationSystem()
                .withPrefixResolver(guild -> new GuildData(dataSource).getPrefix(guild))
                .withCommands(
                        new Channel(dataSource, localizer),
                        new Prefix(dataSource, configuration, localizer),
                        new Reputation(dataSource, localizer),
                        new Roles(dataSource, localizer),
                        new RepSettings(dataSource, localizer),
                        new Thankwords(dataSource, localizer),
                        new Scan(dataSource, localizer)
                )
                .withInvalidArgumentProvider(((loc, command) -> new EmbedBuilder()
                        .setTitle(loc.localize("error.invalidArguments"))
                        .appendDescription(command.getArgs() != null ? command.getCommand() + " " + command.getArgs() + "\n" : "")
                        .appendDescription(">>> " + Arrays.stream(command.getSubCommands())
                                .map(c -> command.getCommand() + " " + c.getName() + (c.getArgs() == null ? "" : c.getArgs()))
                                .collect(Collectors.joining("\n")))
                        .build()))
                .withLocalizer(localizer)
                .build();
        hub.registerCommands(new Help(hub, localizer));
    }

    private void initShutdownHook() {
        var shutdown = new Thread(() -> {
            shardManager.shutdown();
            dataSource.close();
        });
        Runtime.getRuntime().addShutdownHook(shutdown);
    }

    private void initJDA() throws LoginException {
        shardManager = DefaultShardManagerBuilder.createDefault(configuration.get(ConfigFile::getToken))
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_EMOJIS)
                .enableCache(CacheFlag.EMOTE)
                .setEnableShutdownHook(false)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setEventPool(executorService)
                .build();
    }

    private void initConnectionPool() {
        var db = configuration.get(ConfigFile::getDatabase);
        var props = new Properties();
        props.setProperty("dataSourceClassName", PGSimpleDataSource.class.getName());
        props.setProperty("dataSource.serverName", db.getHost());
        props.setProperty("dataSource.portNumber", db.getPort());
        props.setProperty("dataSource.user", db.getUser());
        props.setProperty("dataSource.password", db.getPassword());
        props.setProperty("dataSource.databaseName", db.getDatabase());

        var config = new HikariConfig(props);
        config.setMaximumPoolSize(db.getPoolSize());
        config.setSchema(db.getSchema());

        dataSource = new HikariDataSource(config);
    }
}
