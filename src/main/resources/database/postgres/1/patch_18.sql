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
