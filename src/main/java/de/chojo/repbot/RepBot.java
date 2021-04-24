package de.chojo.repbot;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.chojo.jdautil.listener.CommandListener;
import de.chojo.repbot.commands.Channel;
import de.chojo.repbot.commands.Prefix;
import de.chojo.repbot.commands.Reputation;
import de.chojo.repbot.commands.Roles;
import de.chojo.repbot.config.ConfigFile;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.data.GuildData;
import de.chojo.repbot.listener.MessageListener;
import de.chojo.repbot.listener.ReactionListener;
import de.chojo.repbot.listener.StateListener;
import de.chojo.repbot.manager.RoleAssigner;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.postgresql.ds.PGSimpleDataSource;

import javax.security.auth.login.LoginException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class RepBot {
    private static RepBot instance;
    private final ExecutorService executorService = Executors.newFixedThreadPool(30);
    private ShardManager shardManager;
    private HikariDataSource dataSource;
    private Configuration configuration;

    public static void main(String[] args) {
        RepBot.instance = new RepBot();
        instance.start();
    }

    private void start() {
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

        log.info("Creating Shutdown Hook");
        initShutdownHook();

        log.info("Initializing bot.");
        initBot();
    }

    private void initBot() {
        var roleAssigner = new RoleAssigner(dataSource);

        shardManager.addEventListener(
                new MessageListener(dataSource, configuration, roleAssigner),
                new StateListener(dataSource),
                new ReactionListener(dataSource, roleAssigner));

        CommandListener.builder(shardManager, configuration.get().getDefaultPrefix())
                .receiveGuildMessage()
                .receiveGuildMessagesUpdates()
                .withConversationSystem()
                .withPrefixResolver(guild -> new GuildData(dataSource).getPrefix(guild))
                .withCommands(
                        new Channel(dataSource),
                        new Prefix(dataSource, configuration),
                        new Reputation(dataSource),
                        new Roles(dataSource)
                )
                .build();
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
