/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access;

import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.util.LogNotify;
import de.chojo.sadu.queries.api.query.Query;
import org.slf4j.Logger;

import static de.chojo.sadu.queries.api.call.Call.call;
import static org.slf4j.LoggerFactory.getLogger;

public class Analyzer {
    private static final Logger log = getLogger(Analyzer.class);
    private final Configuration configuration;

    public Analyzer(Configuration configuration) {
        this.configuration = configuration;
    }

    public void cleanup() {
        var delete = Query.query("""
                                  DELETE FROM analyzer_results WHERE analyzed < now() - ?::INTERVAL;
                                  """)
                          .single(call().bind("%d HOURS".formatted(configuration.cleanup().analyzerLogHours())))
                          .delete();
        if (delete.changed()) {
            log.debug("Deleted {} entries from analyzer log", delete.rows());
        } else {
            // TODO log error
            log.error(LogNotify.NOTIFY_ADMIN, "Could not cleanup analyzer log.", delete);
        }
        delete = Query.query("DELETE FROM reputation_results WHERE submitted < now() - ?::INTERVAL")
                      .single(call().bind("%d HOURS".formatted(configuration.cleanup().analyzerLogHours())))
                      .delete();
        if (delete.changed()) {
            log.debug("Deleted {} entries from reputation results", delete.rows());
        } else {
            log.error(LogNotify.NOTIFY_ADMIN, "Could not cleanup analyzer log.", delete);
        }
    }
}
