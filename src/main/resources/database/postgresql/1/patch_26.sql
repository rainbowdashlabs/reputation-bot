ALTER TABLE repbot_schema.reputation_offset
    ADD added TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'utc') NOT NULL;

ALTER TABLE repbot_schema.user_reputation_month
    RENAME TO user_reputation_30_days;

ALTER TABLE repbot_schema.user_reputation_week
    RENAME TO user_reputation_7_days;

DROP VIEW repbot_schema.user_reputation_7_days;
-- Add offset
CREATE OR REPLACE VIEW repbot_schema.user_reputation_7_days AS
WITH rep_offset
         AS (SELECT o.guild_id,
                    o.user_id,
                    SUM(o.amount) AS reputation
             FROM repbot_schema.reputation_offset o
             WHERE o.added > NOW() - INTERVAL '1 week'
             GROUP BY o.guild_id, o.user_id),
     raw_log
         AS (SELECT r.guild_id,
                    r.receiver_id,
                    r.donor_id
             FROM repbot_schema.reputation_log r
             WHERE r.received > NOW() - INTERVAL '1 week'),
     rep_count
         AS (SELECT r.guild_id,
                    r.receiver_id,
                    COUNT(1) AS reputation
             FROM raw_log r
             GROUP BY r.guild_id, r.receiver_id),
     don_count
         AS (SELECT r.guild_id,
                    r.donor_id,
                    COUNT(1) AS donated
             FROM raw_log r
             GROUP BY r.guild_id, r.donor_id),
     -- Build raw log with aggregated user reputation
     full_log
         AS (SELECT COALESCE(don.guild_id, rep.guild_id)    AS guild_id,
                    COALESCE(rep.receiver_id, don.donor_id) AS user_id,
                    COALESCE(rep.reputation, 0::BIGINT)     AS reputation,
                    COALESCE(don.donated, 0::BIGINT)        AS donated
             FROM rep_count rep
                      FULL JOIN don_count don ON rep.guild_id = don.guild_id AND rep.receiver_id = don.donor_id),
     filtered_log
         AS (SELECT guild_id,
                    user_id,
                    reputation,
                    donated
             FROM full_log
             WHERE
               -- Remove entries scheduled for cleanup
                 NOT EXISTS(SELECT 1
                            FROM repbot_schema.cleanup_schedule clean
                            WHERE clean.guild_id = full_log.guild_id
                              AND clean.user_id = full_log.user_id
                     )
               -- remove null users
               AND user_id IS NOT NULL)
SELECT RANK() OVER (PARTITION BY rank.guild_id ORDER BY rank.reputation DESC) AS rank,
       RANK() OVER (PARTITION BY rank.guild_id ORDER BY donated DESC)         AS rank_donated,
       rank.guild_id,
       rank.user_id,
       rank.reputation                                                        AS raw_reputation,
       donated,
       COALESCE(o.reputation, 0)                                              AS rep_offset,
       rank.reputation + COALESCE(o.reputation, 0)                            AS reputation
FROM filtered_log rank
         LEFT JOIN rep_offset o ON rank.guild_id = o.guild_id AND rank.user_id = o.guild_id;

DROP VIEW repbot_schema.user_reputation_30_days;
-- Add offset
CREATE OR REPLACE VIEW repbot_schema.user_reputation_30_days AS
WITH rep_offset
         AS (SELECT o.guild_id,
                    o.user_id,
                    SUM(o.amount) AS reputation
             FROM repbot_schema.reputation_offset o
             WHERE o.added >= NOW() - INTERVAL '30 days'
             GROUP BY o.guild_id, o.user_id),
     raw_log
         AS (SELECT r.guild_id,
                    r.receiver_id,
                    r.donor_id
             FROM repbot_schema.reputation_log r
             WHERE r.received >= NOW() - INTERVAL '30 days'),
     rep_count
         AS (SELECT r.guild_id,
                    r.receiver_id,
                    COUNT(1) AS reputation
             FROM raw_log r
             GROUP BY r.guild_id, r.receiver_id),
     don_count
         AS (SELECT r.guild_id,
                    r.donor_id,
                    COUNT(1) AS donated
             FROM raw_log r
             GROUP BY r.guild_id, r.donor_id),
     full_log
         -- Build raw log with aggregated user reputation
         AS (SELECT COALESCE(don.guild_id, rep.guild_id)    AS guild_id,
                    COALESCE(rep.receiver_id, don.donor_id) AS user_id,
                    COALESCE(rep.reputation, 0::BIGINT)     AS reputation,
                    COALESCE(don.donated, 0::BIGINT)        AS donated
             FROM rep_count rep
                      FULL JOIN don_count don ON rep.guild_id = don.guild_id AND rep.receiver_id = don.donor_id),
     filtered_log AS (SELECT guild_id,
                             user_id,
                             reputation,
                             donated
                      FROM full_log
                      WHERE
                        -- Remove entries scheduled for cleanup
                          NOT EXISTS(SELECT 1
                                     FROM repbot_schema.cleanup_schedule clean
                                     WHERE clean.guild_id = full_log.guild_id
                                       AND clean.user_id = full_log.user_id
                              )
                        -- remove null users
                        AND user_id IS NOT NULL)
SELECT RANK() OVER (PARTITION BY rank.guild_id ORDER BY rank.reputation DESC) AS rank,
       RANK() OVER (PARTITION BY rank.guild_id ORDER BY donated DESC)         AS rank_donated,
       rank.guild_id,
       rank.user_id,
       rank.reputation                                                        AS raw_reputation,
       donated,
       COALESCE(o.reputation, 0)                                              AS rep_offset,
       rank.reputation + COALESCE(o.reputation, 0)                            AS reputation
FROM filtered_log rank
         LEFT JOIN rep_offset o ON rank.guild_id = o.guild_id AND rank.user_id = o.guild_id;

CREATE OR REPLACE VIEW repbot_schema.user_reputation_month AS
WITH rep_offset
         AS (SELECT o.guild_id,
                    o.user_id,
                    SUM(o.amount) AS reputation
             FROM repbot_schema.reputation_offset o
             WHERE DATE_TRUNC('month', o.added) >= DATE_TRUNC('month', NOW())
             GROUP BY o.guild_id, o.user_id),
     raw_log
         AS (SELECT r.guild_id,
                    r.receiver_id,
                    r.donor_id
             FROM repbot_schema.reputation_log r
             WHERE DATE_TRUNC('month', r.received) >= DATE_TRUNC('month', NOW())),
     rep_count
         AS (SELECT r.guild_id,
                    r.receiver_id,
                    COUNT(1) AS reputation
             FROM raw_log r
             GROUP BY r.guild_id, r.receiver_id),
     don_count
         AS (SELECT r.guild_id,
                    r.donor_id,
                    COUNT(1) AS donated
             FROM raw_log r
             GROUP BY r.guild_id, r.donor_id),
     full_log
         -- Build raw log with aggregated user reputation
         AS (SELECT COALESCE(don.guild_id, rep.guild_id)    AS guild_id,
                    COALESCE(rep.receiver_id, don.donor_id) AS user_id,
                    COALESCE(rep.reputation, 0::BIGINT)     AS reputation,
                    COALESCE(don.donated, 0::BIGINT)        AS donated
             FROM rep_count rep
                      FULL JOIN don_count don ON rep.guild_id = don.guild_id AND rep.receiver_id = don.donor_id),
     filtered_log AS (SELECT guild_id,
                             user_id,
                             reputation,
                             donated
                      FROM full_log
                      WHERE
                        -- Remove entries scheduled for cleanup
                          NOT EXISTS(SELECT 1
                                     FROM repbot_schema.cleanup_schedule clean
                                     WHERE clean.guild_id = full_log.guild_id
                                       AND clean.user_id = full_log.user_id
                              )
                        -- remove null users
                        AND user_id IS NOT NULL)
SELECT RANK() OVER (PARTITION BY rank.guild_id ORDER BY rank.reputation DESC) AS rank,
       RANK() OVER (PARTITION BY rank.guild_id ORDER BY donated DESC)         AS rank_donated,
       rank.guild_id,
       rank.user_id,
       rank.reputation                                                        AS raw_reputation,
       donated,
       COALESCE(o.reputation, 0)                                              AS rep_offset,
       rank.reputation + COALESCE(o.reputation, 0)                            AS reputation
FROM filtered_log rank
         LEFT JOIN rep_offset o ON rank.guild_id = o.guild_id AND rank.user_id = o.guild_id;

CREATE OR REPLACE VIEW repbot_schema.user_reputation_week AS
WITH rep_offset
         AS (SELECT o.guild_id,
                    o.user_id,
                    SUM(o.amount) AS reputation
             FROM repbot_schema.reputation_offset o
             WHERE DATE_TRUNC('week', o.added) >= DATE_TRUNC('week', NOW())
             GROUP BY o.guild_id, o.user_id),
     raw_log
         AS (SELECT r.guild_id,
                    r.receiver_id,
                    r.donor_id
             FROM repbot_schema.reputation_log r
             WHERE DATE_TRUNC('week', r.received) >= DATE_TRUNC('week', NOW())),
     rep_count
         AS (SELECT r.guild_id,
                    r.receiver_id,
                    COUNT(1) AS reputation
             FROM raw_log r
             GROUP BY r.guild_id, r.receiver_id),
     don_count
         AS (SELECT r.guild_id,
                    r.donor_id,
                    COUNT(1) AS donated
             FROM raw_log r
             GROUP BY r.guild_id, r.donor_id),
     full_log
         -- Build raw log with aggregated user reputation
         AS (SELECT COALESCE(don.guild_id, rep.guild_id)    AS guild_id,
                    COALESCE(rep.receiver_id, don.donor_id) AS user_id,
                    COALESCE(rep.reputation, 0::BIGINT)     AS reputation,
                    COALESCE(don.donated, 0::BIGINT)        AS donated
             FROM rep_count rep
                      FULL JOIN don_count don ON rep.guild_id = don.guild_id AND rep.receiver_id = don.donor_id),
     filtered_log AS (SELECT guild_id,
                             user_id,
                             reputation,
                             donated
                      FROM full_log
                      WHERE
                        -- Remove entries scheduled for cleanup
                          NOT EXISTS(SELECT 1
                                     FROM repbot_schema.cleanup_schedule clean
                                     WHERE clean.guild_id = full_log.guild_id
                                       AND clean.user_id = full_log.user_id
                              )
                        -- remove null users
                        AND user_id IS NOT NULL)
SELECT RANK() OVER (PARTITION BY rank.guild_id ORDER BY rank.reputation DESC) AS rank,
       RANK() OVER (PARTITION BY rank.guild_id ORDER BY donated DESC)         AS rank_donated,
       rank.guild_id,
       rank.user_id,
       rank.reputation                                                        AS raw_reputation,
       donated,
       COALESCE(o.reputation, 0)                                              AS rep_offset,
       rank.reputation + COALESCE(o.reputation, 0)                            AS reputation
FROM filtered_log rank
         LEFT JOIN rep_offset o ON rank.guild_id = o.guild_id AND rank.user_id = o.guild_id;

CREATE INDEX reputation_log_received_week_index
    ON repbot_schema.reputation_log (DATE_TRUNC('week', received));

CREATE INDEX reputation_log_received_month_index
    ON repbot_schema.reputation_log (DATE_TRUNC('month', received));
