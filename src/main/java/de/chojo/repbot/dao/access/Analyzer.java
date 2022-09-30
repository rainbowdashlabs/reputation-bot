package de.chojo.repbot.dao.access;

import de.chojo.jdautil.util.Futures;
import de.chojo.repbot.util.LogNotify;
import de.chojo.sadu.base.QueryFactory;
import org.slf4j.Logger;

import javax.sql.DataSource;

import static org.slf4j.LoggerFactory.getLogger;

public class Analyzer extends QueryFactory {
    private static final Logger log = getLogger(Analyzer.class);

    public Analyzer(DataSource dataSource) {
        super(dataSource);
    }

    public void cleanup() {
        builder().query("""
                        DELETE FROM analyzer_results WHERE analyzed < NOW() - '24 hours';
                        """)
                 .emptyParams()
                 .delete()
                 .send()
                 .whenComplete(Futures.whenComplete(res -> log.debug("Deleted {} entries from analyzer log", res.rows()),
                         err -> log.error(LogNotify.NOTIFY_ADMIN, "Could not cleanup analyzer log.", err)));
    }
}
