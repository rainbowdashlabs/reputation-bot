package de.chojo.repbot.dao.access;

import de.chojo.jdautil.util.Futures;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.util.LogNotify;
import de.chojo.sadu.base.QueryFactory;
import org.slf4j.Logger;

import javax.sql.DataSource;

import static org.slf4j.LoggerFactory.getLogger;

public class Analyzer extends QueryFactory {
    private static final Logger log = getLogger(Analyzer.class);
    private final Configuration configuration;

    public Analyzer(DataSource dataSource, Configuration configuration) {
        super(dataSource);
        this.configuration = configuration;
    }

    public void cleanup() {
        builder().query("""
                        DELETE FROM analyzer_results WHERE analyzed < NOW() - ?::interval;
                        """)
                .parameter(stmt -> stmt.setString("%d HOURS".formatted(configuration.cleanup().analyzerLogHours())))
                .delete()
                .send()
                .whenComplete(Futures.whenComplete(res -> log.debug("Deleted {} entries from analyzer log", res.rows()),
                        err -> log.error(LogNotify.NOTIFY_ADMIN, "Could not cleanup analyzer log.", err)));
        builder().query("DELETE FROM reputation_results WHERE submitted < now() - ?::interval")
                .parameter(stmt -> stmt.setString("%d HOURS".formatted(configuration.cleanup().analyzerLogHours())))
                .delete()
                .send()
                .whenComplete(Futures.whenComplete(res -> log.debug("Deleted {} entries from reputation results", res.rows()),
                        err -> log.error(LogNotify.NOTIFY_ADMIN, "Could not cleanup reputation results.", err)));
    }
}
