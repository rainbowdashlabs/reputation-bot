ALTER TABLE repbot_schema.reputation_offset
    DROP CONSTRAINT reputation_offset_pk;

CREATE INDEX reputation_offset_guild_id_user_id_index
    ON repbot_schema.reputation_offset (guild_id, user_id);

ALTER TABLE repbot_schema.reputation_offset
    ADD added TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'utc') NOT NULL;

ALTER TABLE repbot_schema.user_reputation_month
    RENAME TO user_reputation_30_days;

ALTER TABLE repbot_schema.user_reputation_week
    RENAME TO user_reputation_7_days;

DROP VIEW IF EXISTS repbot_schema.user_reputation_7_days;
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
               AND user_id IS NOT NULL),
     offset_reputation
         AS (SELECT COALESCE(f.guild_id, o.guild_id)                      AS guild_id,
                    COALESCE(f.user_id, o.user_id)                        AS user_id,
                    -- apply offset to the normal reputation.
                    COALESCE(f.reputation, 0) + COALESCE(o.reputation, 0) AS reputation,
                    COALESCE(o.reputation, 0)                             AS rep_offset,
                    -- save raw reputation without the offset.
                    COALESCE(f.reputation, 0)                             AS raw_reputation,
                    COALESCE(f.donated, 0)                                AS donated
             FROM filtered_log f
                      FULL JOIN rep_offset o ON f.guild_id = o.guild_id AND f.user_id = o.user_id)
SELECT RANK() OVER (PARTITION BY guild_id ORDER BY reputation DESC) AS rank,
       RANK() OVER (PARTITION BY guild_id ORDER BY donated DESC)    AS rank_donated,
       guild_id,
       user_id,
       raw_reputation                                               AS raw_reputation,
       donated,
       rep_offset::BIGINT                                           AS rep_offset,
       reputation::BIGINT                                           AS reputation
FROM offset_reputation rank;

DROP VIEW IF EXISTS repbot_schema.user_reputation_30_days;
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
                        AND user_id IS NOT NULL),
     offset_reputation
         AS (SELECT COALESCE(f.guild_id, o.guild_id)                      AS guild_id,
                    COALESCE(f.user_id, o.user_id)                        AS user_id,
                    -- apply offset to the normal reputation.
                    COALESCE(f.reputation, 0) + COALESCE(o.reputation, 0) AS reputation,
                    COALESCE(o.reputation, 0)                             AS rep_offset,
                    -- save raw reputation without the offset.
                    COALESCE(f.reputation, 0)                             AS raw_reputation,
                    COALESCE(f.donated, 0)                                AS donated
             FROM filtered_log f
                      FULL JOIN rep_offset o ON f.guild_id = o.guild_id AND f.user_id = o.user_id)
SELECT RANK() OVER (PARTITION BY guild_id ORDER BY reputation DESC) AS rank,
       RANK() OVER (PARTITION BY guild_id ORDER BY donated DESC)    AS rank_donated,
       guild_id,
       user_id,
       raw_reputation                                               AS raw_reputation,
       donated,
       rep_offset::BIGINT                                           AS rep_offset,
       reputation::BIGINT                                           AS reputation
FROM offset_reputation rank;

DROP VIEW IF EXISTS repbot_schema.user_reputation_month;
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
                        AND user_id IS NOT NULL),
     offset_reputation
         AS (SELECT COALESCE(f.guild_id, o.guild_id)                      AS guild_id,
                    COALESCE(f.user_id, o.user_id)                        AS user_id,
                    -- apply offset to the normal reputation.
                    COALESCE(f.reputation, 0) + COALESCE(o.reputation, 0) AS reputation,
                    COALESCE(o.reputation, 0)                             AS rep_offset,
                    -- save raw reputation without the offset.
                    COALESCE(f.reputation, 0)                             AS raw_reputation,
                    COALESCE(f.donated, 0)                                AS donated
             FROM filtered_log f
                      FULL JOIN rep_offset o ON f.guild_id = o.guild_id AND f.user_id = o.user_id)
SELECT RANK() OVER (PARTITION BY guild_id ORDER BY reputation DESC) AS rank,
       RANK() OVER (PARTITION BY guild_id ORDER BY donated DESC)    AS rank_donated,
       guild_id,
       user_id,
       raw_reputation                                               AS raw_reputation,
       donated,
       rep_offset::BIGINT                                           AS rep_offset,
       reputation::BIGINT                                           AS reputation
FROM offset_reputation rank;

DROP VIEW IF EXISTS repbot_schema.user_reputation_week;
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
                        AND user_id IS NOT NULL),
     offset_reputation
         AS (SELECT COALESCE(f.guild_id, o.guild_id)                      AS guild_id,
                    COALESCE(f.user_id, o.user_id)                        AS user_id,
                    -- apply offset to the normal reputation.
                    COALESCE(f.reputation, 0) + COALESCE(o.reputation, 0) AS reputation,
                    COALESCE(o.reputation, 0)                             AS rep_offset,
                    -- save raw reputation without the offset.
                    COALESCE(f.reputation, 0)                             AS raw_reputation,
                    COALESCE(f.donated, 0)                                AS donated
             FROM filtered_log f
                      FULL JOIN rep_offset o ON f.guild_id = o.guild_id AND f.user_id = o.user_id)
SELECT RANK() OVER (PARTITION BY guild_id ORDER BY reputation DESC) AS rank,
       RANK() OVER (PARTITION BY guild_id ORDER BY donated DESC)    AS rank_donated,
       guild_id,
       user_id,
       raw_reputation                                               AS raw_reputation,
       donated,
       rep_offset::BIGINT                                           AS rep_offset,
       reputation::BIGINT                                           AS reputation
FROM offset_reputation rank;

CREATE INDEX reputation_log_received_week_index
    ON repbot_schema.reputation_log (DATE_TRUNC('week', received));

CREATE INDEX reputation_log_received_month_index
    ON repbot_schema.reputation_log (DATE_TRUNC('month', received));

-- Add user with offset, but no reputation
CREATE OR REPLACE VIEW repbot_schema.user_reputation
            (rank, rank_donated, guild_id, user_id, reputation, rep_offset, raw_reputation, donated) AS
    -- Prefilter reputation log based on the reset date.
WITH reputation AS (SELECT l.guild_id, l.receiver_id, l.donor_id
                    FROM repbot_schema.reputation_log l
                             LEFT JOIN repbot_schema.guild_settings s ON l.guild_id = s.guild_id
                    WHERE s.reset_date IS NULL
                       OR l.received > s.reset_date
                       OR s.reset_date > now()::DATE),
     rep_count AS (SELECT r.guild_id,
                          r.receiver_id,
                          COUNT(1) AS reputation
                   FROM reputation r
                   GROUP BY r.guild_id, r.receiver_id),
     don_count AS (SELECT r.guild_id,
                          r.donor_id,
                          COUNT(1) AS donated
                   FROM reputation r
                   GROUP BY r.guild_id, r.donor_id),
     raw_log
         -- Build raw log with aggregated user reputation
         AS (SELECT COALESCE(don.guild_id, rep.guild_id)    AS guild_id,
                    COALESCE(rep.receiver_id, don.donor_id) AS user_id,
                    COALESCE(rep.reputation, 0::BIGINT)     AS reputation,
                    COALESCE(don.donated, 0::BIGINT)        AS donated
             FROM rep_count rep
                      FULL JOIN don_count don ON rep.guild_id = don.guild_id AND rep.receiver_id = don.donor_id),
     -- Removes entries which should not be there
     filtered_log
         AS (SELECT *
             FROM raw_log log
             WHERE
               -- Remove entries scheduled for cleanup
                 NOT EXISTS(SELECT 1
                            FROM repbot_schema.cleanup_schedule clean
                            WHERE clean.guild_id = log.guild_id
                              AND clean.user_id = log.user_id
                     )
               -- remove null users
               AND user_id IS NOT NULL),
     -- Add offset and add users which have an offset, but no reputation via full join
     offset_reputation
         AS (SELECT COALESCE(f.guild_id, o.guild_id)                  AS guild_id,
                    COALESCE(f.user_id, o.user_id)                    AS user_id,
                    -- apply offset to the normal reputation.
                    COALESCE(f.reputation, 0) + COALESCE(o.amount, 0) AS reputation,
                    COALESCE(o.amount, 0)                             AS rep_offset,
                    -- save raw reputation without the offset.
                    COALESCE(f.reputation, 0)                         AS raw_reputation,
                    COALESCE(f.donated, 0)                            AS donated
             FROM filtered_log f
                      FULL JOIN repbot_schema.reputation_offset o ON f.guild_id = o.guild_id AND f.user_id = o.user_id)
-- Build the ranking
SELECT RANK() OVER (PARTITION BY guild_id ORDER BY reputation DESC) AS rank,
       RANK() OVER (PARTITION BY guild_id ORDER BY donated DESC)    AS rank_donated,
       guild_id,
       user_id,
       reputation,
       rep_offset,
       raw_reputation,
       donated
FROM offset_reputation;
