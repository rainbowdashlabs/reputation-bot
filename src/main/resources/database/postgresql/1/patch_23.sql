-- feature/guild-cleanup
CREATE OR REPLACE VIEW repbot_schema.guilds AS
SELECT guild_id
FROM repbot_schema.reputation_log
UNION
DISTINCT
SELECT guild_id
FROM repbot_schema.abuse_protection
UNION
DISTINCT
SELECT guild_id
FROM repbot_schema.active_categories
UNION
DISTINCT
SELECT guild_id
FROM repbot_schema.active_channel
UNION
DISTINCT
SELECT guild_id
FROM repbot_schema.announcements
UNION
DISTINCT
SELECT guild_id
FROM repbot_schema.donor_roles
UNION
DISTINCT
SELECT guild_id
FROM repbot_schema.guild_ranks
UNION
DISTINCT
SELECT guild_id
FROM repbot_schema.guild_reactions
UNION
DISTINCT
SELECT guild_id
FROM repbot_schema.guild_settings
UNION
DISTINCT
SELECT guild_id
FROM repbot_schema.message_states
UNION
DISTINCT
SELECT guild_id
FROM repbot_schema.receiver_roles
UNION
DISTINCT
SELECT guild_id
FROM repbot_schema.donor_roles
UNION
DISTINCT
SELECT guild_id
FROM repbot_schema.reputation_offset
UNION
DISTINCT
SELECT guild_id
FROM repbot_schema.reputation_settings
UNION
DISTINCT
SELECT guild_id
FROM repbot_schema.thank_settings
UNION
DISTINCT
SELECT guild_id
FROM repbot_schema.thankwords
UNION
DISTINCT
SELECT guild_id
FROM repbot_schema.voice_activity
ORDER BY guild_id;

UPDATE repbot_schema.cleanup_schedule
SET user_id = 0
WHERE user_id IS NULL;

-- feature/analyzer-log
CREATE TABLE repbot_schema.analyzer_results
(
    guild_id   BIGINT                                       NOT NULL,
    channel_id BIGINT                                       NOT NULL,
    message_id BIGINT                                       NOT NULL,
    result     jsonb                                        NOT NULL,
    analyzed   TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'utc') NOT NULL,
    CONSTRAINT analyzer_results_pk
        PRIMARY KEY (guild_id, message_id)
);

CREATE INDEX analyzer_results_guild_id_index
    ON repbot_schema.analyzer_results (guild_id);

CREATE INDEX analyzer_results_analyzed_index
    ON repbot_schema.analyzer_results (analyzed);

-- feature/reset-date
ALTER TABLE repbot_schema.guild_settings
    ADD reset_date DATE;

-- Add user with offset, but no reputation
CREATE OR REPLACE VIEW repbot_schema.user_reputation
            (rank, rank_donated, guild_id, user_id, reputation, rep_offset, raw_reputation, donated) AS
    -- Prefilter reputation log based on the reset date.
WITH reputation AS (SELECT l.guild_id, l.receiver_id, l.donor_id
                    FROM repbot_schema.reputation_log l
                             LEFT JOIN repbot_schema.guild_settings s ON l.guild_id = s.guild_id
                    WHERE s.reset_date IS NULL
                       OR l.received > s.reset_date),
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

-- feature/static-metrics
-- Create a new table containing the reputation of each day grouped by day
CREATE TABLE repbot_schema.metrics_reputation
(
    day   DATE   NOT NULL,
    cause TEXT   NOT NULL,
    count BIGINT NOT NULL
);

CREATE INDEX metrics_reputation_day_index
    ON repbot_schema.metrics_reputation (day);

ALTER TABLE repbot_schema.metrics_reputation
    ADD CONSTRAINT metrics_reputation_pk
        PRIMARY KEY (day, cause);

-- prepopulate the table with data from the not current day. After this the metric service takes over.
INSERT INTO repbot_schema.metrics_reputation
SELECT received::DATE AS day,
       cause,
       COUNT(1)       AS count
FROM repbot_schema.reputation_log
WHERE received::DATE != NOW()::DATE
GROUP BY day, cause
ORDER BY day DESC;

-- We now need to recreate all views which were based on the reputation log itself
CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_type_week AS
SELECT DATE_TRUNC('week', day)::DATE AS week,
       cause,
       SUM(count)::BIGINT            AS count
FROM repbot_schema.metrics_reputation
GROUP BY week, cause
ORDER BY week DESC, cause;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_type_month AS
SELECT DATE_TRUNC('month', day)::DATE AS month,
       cause,
       SUM(count)::BIGINT             AS count
FROM repbot_schema.metrics_reputation
GROUP BY month, cause
ORDER BY month DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_dow_week AS
SELECT DATE_TRUNC('week', day)::DATE AS week,
       EXTRACT(ISODOW FROM day)      AS dow,
       SUM(count)::BIGINT            AS count
FROM repbot_schema.metrics_reputation
GROUP BY week, dow
ORDER BY week DESC, dow ASC;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_week AS
SELECT DATE_TRUNC('week', day)::DATE AS week,
       SUM(count)::BIGINT            AS count
FROM repbot_schema.metrics_reputation
GROUP BY week
ORDER BY week DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_month AS
SELECT DATE_TRUNC('month', day)::DATE AS month,
       SUM(count)::BIGINT             AS count
FROM repbot_schema.metrics_reputation
GROUP BY month
ORDER BY month DESC;

-- Create a table showing the total of saved reputation in the database
CREATE TABLE repbot_schema.metrics_reputation_count
(
    day     DATE              NOT NULL
        CONSTRAINT metrics_reputation_count_pk
            PRIMARY KEY,
    count   INTEGER DEFAULT 0 NOT NULL,
    added   INTEGER DEFAULT 0 NOT NULL,
    removed INTEGER DEFAULT 0 NOT NULL
);

-- Prepopulate counts
INSERT INTO repbot_schema.metrics_reputation_count
SELECT day,
       SUM(count) OVER (ORDER BY day) AS count
FROM (SELECT received::DATE AS day,
             COUNT(1)       AS count
      FROM repbot_schema.reputation_log
      GROUP BY day
      ORDER BY day DESC) m
WHERE day != NOW()::DATE
ORDER BY day DESC
ON CONFLICT(day) DO NOTHING;



CREATE OR REPLACE FUNCTION repbot_schema.reputation_added()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$BODY$
BEGIN
    INSERT INTO repbot_schema.metrics_reputation_count AS c (day, added)
    VALUES (NOW()::DATE, 1)
    ON CONFLICT(day) DO UPDATE SET added = c.added + 1;
    RETURN new;
END;
$BODY$;

CREATE OR REPLACE FUNCTION repbot_schema.reputation_removed()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$BODY$
BEGIN
    INSERT INTO repbot_schema.metrics_reputation_count AS c (day, removed)
    VALUES (NOW()::DATE, 1)
    ON CONFLICT(day) DO UPDATE SET removed = c.removed + 1;
    RETURN new;
END;
$BODY$;

-- Create triggers to count insert and deletions on reputation table.
CREATE TRIGGER reputation_add
    AFTER INSERT
    ON repbot_schema.reputation_log
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.reputation_added();

CREATE OR REPLACE TRIGGER reputation_remove
    AFTER DELETE
    ON repbot_schema.reputation_log
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.reputation_removed();

-- Create tables for user freezing
CREATE TABLE repbot_schema.metrics_users_week
(
    week           DATE   NOT NULL
        CONSTRAINT metrics_users_week_pk
            PRIMARY KEY,
    donor_count    BIGINT NOT NULL,
    receiver_count BIGINT NOT NULL,
    total_count    BIGINT NOT NULL
);

CREATE TABLE repbot_schema.metrics_users_month
(
    month          DATE   NOT NULL
        CONSTRAINT metrics_users_month_pk
            PRIMARY KEY,
    donor_count    BIGINT NOT NULL,
    receiver_count BIGINT NOT NULL,
    total_count    BIGINT NOT NULL
);

-- Prepopulate tables
INSERT INTO repbot_schema.metrics_users_month
SELECT month, receiver_count, donor_count, total_count
FROM repbot_schema.metrics_unique_users_month;

INSERT INTO repbot_schema.metrics_users_week
SELECT week, receiver_count, donor_count, total_count
FROM repbot_schema.metrics_unique_users_week;

-- Add views on metrics_reputation_count
CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_changed_week AS
SELECT DATE_TRUNC('week', day)::DATE AS week,
       MAX(count)::BIGINT             AS count,
       SUM(added)::BIGINT             AS added,
       MAX(removed)::BIGINT           AS removed
FROM repbot_schema.metrics_reputation_count
GROUP BY week
ORDER BY week DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_changed_month AS
SELECT DATE_TRUNC('month', day)::DATE AS month,
       MAX(count)::BIGINT             AS count,
       SUM(added)::BIGINT             AS added,
       MAX(removed)::BIGINT           AS removed
FROM repbot_schema.metrics_reputation_count
GROUP BY month
ORDER BY month DESC;
