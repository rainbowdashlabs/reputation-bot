package de.chojo.repbot;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.chojo.repbot.config.ConfigFile;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.listener.MessageListener;
import de.chojo.repbot.listener.StateListener;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.postgresql.ds.PGSimpleDataSource;

import javax.security.auth.login.LoginException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class RepBot {
    private static RepBot instance;
    private ShardManager shardManager;
    private HikariDataSource dataSource;
    private Configuration configuration;
    private final ExecutorService executorService = Executors.newFixedThreadPool(30);

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
        shardManager.addEventListener(new MessageListener(dataSource, configuration));
        shardManager.addEventListener(new StateListener(dataSource));
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
                .enableIntents(GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_MESSAGES)
                .enableCache(CacheFlag.EMOTE)
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
