/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.repbot.dao.access;

import de.chojo.jdautil.interactions.premium.SKU;
import de.chojo.repbot.config.Configuration;
import de.chojo.repbot.util.LogNotify;
import de.chojo.sadu.postgresql.types.PostgreSqlTypes;
import de.chojo.sadu.queries.api.query.Query;
import org.slf4j.Logger;

import java.util.List;

import static de.chojo.sadu.queries.api.call.Call.call;
import static org.slf4j.LoggerFactory.getLogger;

public class Analyzer {
    private static final Logger log = getLogger(Analyzer.class);
    private final Configuration configuration;

    public Analyzer(Configuration configuration) {
        this.configuration = configuration;
    }

    public void cleanup() {
        int extendedHours = configuration.skus().features().analyzerLog().extendedLogHours();
        List<Long> skuEntry = configuration.skus().features().analyzerLog().longerLogTime().sku().stream().map(SKU::getSkuIdLong).toList();

        var delete = Query.query("""
                                  WITH
                                      subs AS (
                                          SELECT
                                              id,
                                              sku
                                          FROM
                                              subscriptions
                                          WHERE sku = ANY ( :sku )
                                              ),
                                      target AS (
                                          SELECT
                                              guild_id,
                                              message_id
                                          FROM
                                              analyzer_results a
                                                  LEFT JOIN subs s
                                                  ON a.guild_id = s.id
                                          WHERE analyzed < now() - ( CASE WHEN sku IS NULL THEN :defaultHours ELSE :extendedHours END )::INTERVAL
                                          ORDER BY message_id
                                              FOR UPDATE
                                              )
                                  DELETE
                                  FROM
                                      analyzer_results r
                                      USING target t
                                  WHERE r.message_id = t.message_id;
                                  """)
                          .single(call().bind("defaultHours", "%d HOURS".formatted(configuration.cleanup().analyzerLogHours()))
                                        .bind("extendedHours", "%d HOURS".formatted(extendedHours))
                                        .bind("sku", skuEntry, PostgreSqlTypes.BIGINT))
                          .delete();
        if (delete.changed()) {
            log.debug("Deleted {} entries from analyzer log", delete.rows());
        } else {
            delete.exceptions().forEach(e -> log.error(LogNotify.NOTIFY_ADMIN, "Could not cleanup analyzer log.", e));
        }
        delete = Query.query("""
                              WITH
                                  subs AS (
                                      SELECT
                                          id,
                                          sku
                                      FROM
                                          subscriptions
                                      WHERE sku = ANY ( :sku )
                                          ),
                                  target AS (
                                      SELECT
                                          guild_id,
                                          message_id
                                      FROM
                                          reputation_results a
                                              LEFT JOIN subs s
                                              ON a.guild_id = s.id
                                      WHERE submitted < now() - ( CASE WHEN sku IS NULL THEN :defaultHours ELSE :extendedHours END )::INTERVAL
                                      ORDER BY message_id
                                          FOR UPDATE
                                          )
                              DELETE
                              FROM
                                  reputation_results r
                                  USING target t
                              WHERE r.message_id = t.message_id;
                              """)
                      .single(call().bind("defaultHours", "%d HOURS".formatted(configuration.cleanup().analyzerLogHours()))
                                    .bind("extendedHours", "%d HOURS".formatted(extendedHours))
                                    .bind("sku", skuEntry, PostgreSqlTypes.BIGINT))
                      .delete();
        if (delete.changed()) {
            log.debug("Deleted {} entries from reputation results", delete.rows());
        } else {
            delete.exceptions().forEach(e -> log.error(LogNotify.NOTIFY_ADMIN, "Could not cleanup analyzer log.", e));
        }
    }
}
