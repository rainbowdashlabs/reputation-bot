CREATE TABLE IF NOT EXISTS repbot_schema.active_categories
(
    guild_id    BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    CONSTRAINT active_categories_pk
        PRIMARY KEY (guild_id, category_id)
);

CREATE INDEX IF NOT EXISTS active_categories_guild_id_index
    ON repbot_schema.active_categories (guild_id);

-- We need to drop the old view, because we change the view row layout.
DROP VIEW repbot_schema.user_reputation_week;

-- We add a rank donated and remove null user ids from log
CREATE OR REPLACE VIEW repbot_schema.user_reputation_week(rank, rank_donated, guild_id, user_id, reputation, donated) AS
SELECT ROW_NUMBER() OVER (PARTITION BY rank.guild_id ORDER BY rank.reputation DESC) AS rank,
       ROW_NUMBER() OVER (PARTITION BY rank.guild_id ORDER BY rank.donated DESC)    AS rank_donated,
       rank.guild_id,
       rank.user_id,
       rank.reputation,
       rank.donated
FROM (SELECT log.guild_id,
             log.user_id,
             log.reputation,
             log.donated
      FROM (SELECT COALESCE(don.guild_id, rep.guild_id)    AS guild_id,
                   COALESCE(rep.receiver_id, don.donor_id) AS user_id,
                   COALESCE(rep.reputation, 0::BIGINT)     AS reputation,
                   COALESCE(don.donated, 0::BIGINT)        AS donated
            FROM (SELECT r.guild_id # r.receiver_id AS key,
                         r.guild_id,
                         r.receiver_id,
                         COUNT(1)                   AS reputation
                  FROM repbot_schema.reputation_log r
                  WHERE r.received > NOW() - INTERVAL '1 week'
                  GROUP BY r.guild_id, r.receiver_id) rep
                     FULL JOIN (SELECT r.guild_id # r.donor_id AS key,
                                       r.guild_id,
                                       r.donor_id,
                                       COUNT(1)                AS donated
                                FROM repbot_schema.reputation_log r
                                WHERE r.received > NOW() - INTERVAL '1 week'
                                GROUP BY r.guild_id, r.donor_id) don ON rep.key = don.key) log
      WHERE NOT (EXISTS(SELECT 1
                        FROM repbot_schema.cleanup_schedule clean
                        WHERE clean.guild_id = log.guild_id
                          AND clean.user_id = log.user_id))
        AND user_id IS NOT NULL) rank;

-- We need to drop the old view, because we change the view row layout.
DROP VIEW repbot_schema.user_reputation_month;

-- We add a rank donated and remove null user ids from log
CREATE OR REPLACE VIEW repbot_schema.user_reputation_month(rank, rank_donated, guild_id, user_id, reputation, donated) AS
SELECT ROW_NUMBER() OVER (PARTITION BY rank.guild_id ORDER BY rank.reputation DESC) AS rank,
       ROW_NUMBER() OVER (PARTITION BY rank.guild_id ORDER BY rank.donated DESC)    AS rank_donated,
       rank.guild_id,
       rank.user_id,
       rank.reputation,
       rank.donated
FROM (SELECT log.guild_id,
             log.user_id,
             log.reputation,
             log.donated
      FROM (SELECT COALESCE(don.guild_id, rep.guild_id)    AS guild_id,
                   COALESCE(rep.receiver_id, don.donor_id) AS user_id,
                   COALESCE(rep.reputation, 0::BIGINT)     AS reputation,
                   COALESCE(don.donated, 0::BIGINT)        AS donated
            FROM (SELECT r.guild_id # r.receiver_id AS key,
                         r.guild_id,
                         r.receiver_id,
                         COUNT(1)                   AS reputation
                  FROM repbot_schema.reputation_log r
                  WHERE r.received > NOW() - INTERVAL '4 week'
                  GROUP BY r.guild_id, r.receiver_id) rep
                     FULL JOIN (SELECT r.guild_id # r.donor_id AS key,
                                       r.guild_id,
                                       r.donor_id,
                                       COUNT(1)                AS donated
                                FROM repbot_schema.reputation_log r
                                WHERE r.received > NOW() - INTERVAL '4 week'
                                GROUP BY r.guild_id, r.donor_id) don ON rep.key = don.key) log
      WHERE NOT (EXISTS(SELECT 1
                        FROM repbot_schema.cleanup_schedule clean
                        WHERE clean.guild_id = log.guild_id
                          AND clean.user_id = log.user_id))
        AND user_id IS NOT NULL) rank;

-- We drop global reputation here because it depends on user_reputation.
DROP VIEW repbot_schema.global_user_reputation;

-- We need to drop the old view, because we change the view row layout.
DROP VIEW repbot_schema.user_reputation;

-- We add a rank donated and remove null user ids from log
CREATE OR REPLACE VIEW repbot_schema.user_reputation(rank, rank_donated, guild_id, user_id, reputation, donated) AS
SELECT ROW_NUMBER() OVER (PARTITION BY rank.guild_id ORDER BY rank.reputation DESC) AS rank,
       ROW_NUMBER() OVER (PARTITION BY rank.guild_id ORDER BY rank.donated DESC)    AS rank_donated,
       rank.guild_id,
       rank.user_id,
       rank.reputation,
       rank.donated
FROM (SELECT guild_id,
             user_id,
             reputation,
             donated
      FROM (SELECT COALESCE(don.guild_id, rep.guild_id)    AS guild_id,
                   COALESCE(rep.receiver_id, don.donor_id) AS user_id,
                   COALESCE(rep.reputation, 0::BIGINT)     AS reputation,
                   COALESCE(don.donated, 0::BIGINT)        AS donated
            FROM (SELECT r.guild_id # r.receiver_id AS key,
                         r.guild_id,
                         r.receiver_id,
                         COUNT(1)                   AS reputation
                  FROM repbot_schema.reputation_log r
                  GROUP BY r.guild_id, r.receiver_id) rep
                     FULL JOIN (SELECT r.guild_id # r.donor_id AS key,
                                       r.guild_id,
                                       r.donor_id,
                                       COUNT(1)                AS donated
                                FROM repbot_schema.reputation_log r
                                GROUP BY r.guild_id, r.donor_id) don
                               ON rep.key = don.key) log
      WHERE NOT EXISTS(SELECT 1
                       FROM repbot_schema.cleanup_schedule clean
                       WHERE clean.guild_id = log.guild_id
                         AND clean.user_id = log.user_id)
        AND user_id IS NOT NULL) rank;

-- Adds a ranking for donated
CREATE OR REPLACE VIEW repbot_schema.global_user_reputation(rank, rank_donated, user_id, reputation, donated) AS
SELECT ROW_NUMBER() OVER (ORDER BY reputation DESC) AS rank,
       ROW_NUMBER() OVER (ORDER BY donated DESC)    AS rank_donated,
       user_id,
       reputation,
       donated
FROM (SELECT user_reputation.user_id,
             SUM(user_reputation.reputation) AS reputation,
             SUM(user_reputation.donated)    AS donated
      FROM repbot_schema.user_reputation
      GROUP BY user_reputation.user_id) rep;

-- We still have some old entries which might have a 0 donor id instead of the null.
-- We will finally remove this inconsistency.
UPDATE repbot_schema.reputation_log SET donor_id = NULL WHERE donor_id = 0;

CREATE TABLE IF NOT EXISTS repbot_schema.announcements
(
    guild_id     BIGINT
        CONSTRAINT announcements_pk
            PRIMARY KEY,
    active       BOOLEAN DEFAULT FALSE NOT NULL,
    same_channel BOOLEAN DEFAULT TRUE  NOT NULL,
    channel_id   BIGINT
);

-- Finally get rid of this stupid migration mechanic
DROP TABLE IF EXISTS repbot_schema.migrations;

CREATE TABLE IF NOT EXISTS repbot_schema.metrics_commands
(
    day     DATE          NOT NULL,
    command TEXT          NOT NULL,
    count   INT DEFAULT 1 NOT NULL,
    CONSTRAINT metrics_commands_pk
        PRIMARY KEY (day, command)
);

CREATE INDEX IF NOT EXISTS metrics_commands_day_index
    ON repbot_schema.metrics_commands (day);

CREATE OR REPLACE VIEW repbot_schema.metrics_commands_week AS
SELECT DATE_TRUNC('week', day)::DATE AS week, command, SUM(count) AS count
FROM repbot_schema.metrics_commands
GROUP BY week, command;


CREATE OR REPLACE VIEW repbot_schema.metrics_commands_month AS
SELECT DATE_TRUNC('month', day)::DATE AS month, command, SUM(count) AS count
FROM repbot_schema.metrics_commands
GROUP BY month, command;

CREATE TABLE IF NOT EXISTS repbot_schema.metrics_message_analyzed
(
    hour  TIMESTAMP NOT NULL
        CONSTRAINT metric_message_analyzed_pk
            PRIMARY KEY,
    count INT       NOT NULL
);

CREATE OR REPLACE VIEW repbot_schema.metrics_message_analyzed_day AS
SELECT DATE_TRUNC('day', hour)::DATE AS day,
       SUM(count)                    AS count
FROM repbot_schema.metrics_message_analyzed
GROUP BY day;

CREATE OR REPLACE VIEW repbot_schema.metrics_message_analyzed_week AS
SELECT DATE_TRUNC('week', hour)::DATE AS week,
       SUM(count)                     AS count
FROM repbot_schema.metrics_message_analyzed
GROUP BY week;

CREATE OR REPLACE VIEW repbot_schema.metrics_message_analyzed_month AS
SELECT DATE_TRUNC('month', hour)::DATE AS month,
       SUM(count)                      AS count
FROM repbot_schema.metrics_message_analyzed
GROUP BY month;

-- Weekly metrics about unique donors and receivers
CREATE OR REPLACE VIEW repbot_schema.metrics_unique_users_week AS
WITH received AS (SELECT DATE_TRUNC('week', received)::DATE AS week,
                         receiver_id
                  FROM repbot_schema.reputation_log
                  GROUP BY week, receiver_id),
     received_count AS (SELECT week, COUNT(1)
                        FROM received
                        GROUP BY week),
     donated AS (SELECT DATE_TRUNC('week', received)::DATE AS week,
                        donor_id
                 FROM repbot_schema.reputation_log
                 GROUP BY week, donor_id),
     donated_count AS (SELECT week, COUNT(1)
                       FROM donated
                       GROUP BY week),
     total AS (SELECT *
               FROM received
               UNION
               DISTINCT
               SELECT *
               FROM donated),
     total_count AS (SELECT week, COUNT(1)
                     FROM total
                     GROUP BY week)
SELECT d.week,
       r.count AS receiver_count,
       d.count AS donor_count,
       t.count AS total_count
FROM received_count r
         FULL JOIN donated_count d ON r.week = d.week
         FULL JOIN total_count t ON r.week = t.week
ORDER BY week DESC;

-- Monthly metrics about unique donors and receivers
CREATE OR REPLACE VIEW repbot_schema.metrics_unique_users_month AS
WITH received AS (SELECT DATE_TRUNC('month', received)::DATE AS month,
                         receiver_id                         AS id
                  FROM repbot_schema.reputation_log
                  GROUP BY month, id),
     received_count AS (SELECT month, COUNT(1)
                        FROM received
                        GROUP BY month),
     donated AS (SELECT DATE_TRUNC('month', received)::DATE AS month,
                        donor_id                            AS id
                 FROM repbot_schema.reputation_log
                 GROUP BY month, id),
     donated_count AS (SELECT month, COUNT(1)
                       FROM donated
                       GROUP BY month),
     total AS (SELECT *
               FROM received
               UNION
               DISTINCT
               SELECT *
               FROM donated),
     total_count AS (SELECT month, COUNT(1)
                     FROM total
                     GROUP BY month)
SELECT d.month,
       r.count AS receiver_count,
       d.count AS donor_count,
       t.count AS total_count
FROM received_count r
         FULL JOIN donated_count d ON r.month = d.month
         FULL JOIN total_count t ON r.month = t.month
ORDER BY month DESC;
