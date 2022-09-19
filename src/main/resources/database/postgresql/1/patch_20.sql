-- Add a reputation offset for the total ranking
CREATE TABLE IF NOT EXISTS repbot_schema.reputation_offset
(
    guild_id BIGINT NOT NULL,
    user_id  BIGINT NOT NULL,
    amount   BIGINT NOT NULL,
    CONSTRAINT reputation_offset_pk
        PRIMARY KEY (guild_id, user_id)
);

CREATE INDEX IF NOT EXISTS reputation_offset_guild_id_index
    ON repbot_schema.reputation_offset (guild_id);

-- Add the offset to the ranking
DROP VIEW repbot_schema.user_reputation CASCADE;
CREATE OR REPLACE VIEW repbot_schema.user_reputation
            (rank, rank_donated, guild_id, user_id, reputation, rep_offset, raw_reputation, donated) AS
SELECT ROW_NUMBER() OVER (PARTITION BY rank.guild_id ORDER BY rank.reputation DESC) AS rank,
       ROW_NUMBER() OVER (PARTITION BY rank.guild_id ORDER BY rank.donated DESC)    AS rank_donated,
       rank.guild_id,
       rank.user_id,
       rank.reputation,
       rank.rep_offset,
       rank.raw_reputation,
       rank.donated
FROM (SELECT raw_rep.guild_id,
             raw_rep.user_id,
             -- apply offset to the normal reputation.
             -- This will require nearly no changes anywhere
             raw_rep.reputation + COALESCE(o.amount, 0) AS reputation,
             -- We save the offset on the side and allow retrieval of it
             COALESCE(o.amount, 0)                      AS rep_offset,
             -- save raw reputation without the offset.
             raw_rep.reputation                         AS raw_reputation,
             raw_rep.donated
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
              AND user_id IS NOT NULL) raw_rep
               LEFT JOIN repbot_schema.reputation_offset o
                         ON raw_rep.guild_id = o.guild_id AND raw_rep.user_id = o.user_id) rank;

-- Recreate global reputation because it depends on the user reputation
CREATE OR REPLACE VIEW repbot_schema.global_user_reputation(rank, rank_donated, user_id, reputation, donated) AS
SELECT ROW_NUMBER() OVER (ORDER BY reputation DESC) AS rank,
       ROW_NUMBER() OVER (ORDER BY donated DESC)    AS rank_donated,
       user_id,
       reputation,
       donated
FROM (SELECT user_reputation.user_id,
             -- Use the raw reputation here to ignore the offsets.
             SUM(user_reputation.raw_reputation) AS reputation,
             SUM(user_reputation.donated)        AS donated
      FROM repbot_schema.user_reputation
      GROUP BY user_reputation.user_id) rep;

-- Add new offset to user data
CREATE OR REPLACE FUNCTION repbot_schema.aggregate_user_data(_user_id BIGINT)
    RETURNS TEXT
    LANGUAGE plpgsql
    COST 100
AS
$BODY$
DECLARE
    _result jsonb;
BEGIN
    WITH reputation AS (SELECT JSONB_AGG(
                                       JSONB_BUILD_OBJECT(
                                               'guild', guild_id,
                                               'channel', channel_id,
                                               'donor', CASE WHEN donor_id = _user_id THEN _user_id END,
                                               'receiver', CASE WHEN receiver_id = _user_id THEN _user_id END,
                                               'message', message_id,
                                               'ref_message', ref_message_id,
                                               'cause', cause,
                                               'received', received::TEXT
                                           )
                                   ) AS rep
                        FROM repbot_schema.reputation_log l
                        WHERE l.receiver_id = _user_id
                           OR l.donor_id = _user_id),
         reputation_offset AS (SELECT JSONB_AGG(
                                              JSONB_BUILD_OBJECT(
                                                      'guild', guild_id,
                                                      'user', user_id,
                                                      'amount', amount
                                                  )
                                          ) AS reputation_offset
                               FROM repbot_schema.reputation_offset
                               WHERE user_id = _user_id),
         voice_activity AS (SELECT JSONB_AGG(
                                           JSONB_BUILD_OBJECT(
                                                   'guild', guild_id,
                                                   'user_1', CASE WHEN user_id_1 = _user_id THEN _user_id END,
                                                   'user_2', CASE WHEN user_id_2 = _user_id THEN _user_id END,
                                                   'seen', seen::TEXT
                                               )
                                       ) AS voice
                            FROM repbot_schema.voice_activity
                            WHERE user_id_1 = _user_id
                               OR user_id_2 = _user_id),
         cleanup_tasks AS (SELECT JSONB_AGG(
                                          JSONB_BUILD_OBJECT(
                                                  'guild', guild_id,
                                                  'user', user_id,
                                                  'delete_after', delete_after::TEXT
                                              )
                                      ) AS cleanup
                           FROM repbot_schema.cleanup_schedule c
                           WHERE c.user_id = _user_id),
         gdpr_log AS (SELECT JSONB_BUILD_OBJECT(
                                     'user', user_id,
                                     'received', NOW()::TEXT,
                                     'attempts', attempts,
                                     'requested', requested
                                 ) AS gdpr
                      FROM repbot_schema.gdpr_log l
                      WHERE l.user_id = _user_id)
    SELECT JSONB_BUILD_OBJECT(
                   'reputation', COALESCE(rep, '[]'::jsonb),
                   'voice_activity', COALESCE(voice, '[]'::jsonb),
                   'cleanup_tasks', COALESCE(cleanup, '[]'::jsonb),
                   'gdpr_log', COALESCE(gdpr, '{}'::jsonb),
                   'reputation_offset', COALESCE(reputation_offset, '[]'::jsonb)
               )
    FROM reputation,
         voice_activity,
         cleanup_tasks,
         gdpr_log reputation_offset
    INTO _result;
    RETURN JSONB_PRETTY(_result);
END;
$BODY$;

-- Enhance abuse protection
ALTER TABLE repbot_schema.abuse_protection
    ADD IF NOT EXISTS max_given INT DEFAULT 0 NOT NULL;

ALTER TABLE repbot_schema.abuse_protection
    ADD IF NOT EXISTS max_given_hours INT DEFAULT 1 NOT NULL;

ALTER TABLE repbot_schema.abuse_protection
    ADD IF NOT EXISTS max_received INT DEFAULT 0 NOT NULL;

ALTER TABLE repbot_schema.abuse_protection
    ADD IF NOT EXISTS max_received_hours INT DEFAULT 1 NOT NULL;

ALTER TABLE repbot_schema.abuse_protection
    ADD IF NOT EXISTS max_message_reputation INT DEFAULT 3 NOT NULL;

-- Create metrics for interactions
CREATE TABLE IF NOT EXISTS repbot_schema.metrics_handled_interactions
(
    hour    TIMESTAMP NOT NULL
        CONSTRAINT metrics_handled_interactions_pk
            PRIMARY KEY,
    count   INTEGER   NOT NULL DEFAULT 0,
    failed  INTEGER   NOT NULL DEFAULT 0,
    success INTEGER   NOT NULL DEFAULT 0
);

CREATE OR REPLACE VIEW repbot_schema.metrics_handled_interactions_day AS
SELECT DATE_TRUNC('day', hour)::DATE AS day,
       SUM(count)                    AS count,
       SUM(failed)                   AS failed,
       SUM(success)                  AS success
FROM repbot_schema.metrics_handled_interactions
GROUP BY day;

CREATE OR REPLACE VIEW repbot_schema.metrics_handled_interactions_week AS
SELECT DATE_TRUNC('week', hour)::DATE AS week,
       SUM(count)                     AS count,
       SUM(failed)                    AS failed,
       SUM(success)                   AS success
FROM repbot_schema.metrics_handled_interactions
GROUP BY week;

CREATE OR REPLACE VIEW repbot_schema.metrics_handled_interactions_month AS
SELECT DATE_TRUNC('month', hour)::DATE AS month,
       SUM(count)                      AS count,
       SUM(failed)                     AS failed,
       SUM(success)                    AS success
FROM repbot_schema.metrics_handled_interactions
GROUP BY month;

-- Create metrics for reputation by type
CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_type_week AS
SELECT DATE_TRUNC('week', received)::DATE AS week,
       cause,
       COUNT(1) AS count
FROM repbot_schema.reputation_log
GROUP BY week, cause
ORDER BY week DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_type_month AS
SELECT DATE_TRUNC('month', received)::DATE AS month,
       cause,
       COUNT(1) AS count
FROM repbot_schema.reputation_log
GROUP BY month, cause
ORDER BY month DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_type_total_week AS
SELECT week,
       cause,
       SUM(count) OVER (PARTITION BY cause ORDER BY week) AS count
FROM repbot_schema.metrics_reputation_type_week m
ORDER BY week DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_type_total_month AS
SELECT month,
       cause,
       SUM(count) OVER (PARTITION BY cause ORDER BY month) AS count
FROM repbot_schema.metrics_reputation_type_month m
ORDER BY month DESC;

-- Create table to define the state of several messages
CREATE TABLE IF NOT EXISTS repbot_schema.message_states
(
    guild_id              BIGINT               NOT NULL
        CONSTRAINT messages_pk
            PRIMARY KEY,
    reaction_confirmation BOOLEAN DEFAULT TRUE NOT NULL
);

-- Allow skipping of an embed
ALTER TABLE IF EXISTS repbot_schema.message_settings
    ADD skip_single_embed BOOLEAN DEFAULT FALSE NOT NULL;

-- Add max message reputation
ALTER TABLE IF EXISTS repbot_schema.message_settings
    ADD max_message_reputation INT DEFAULT 3 NOT NULL;

-- Rename for better consistency with dao
ALTER TABLE IF EXISTS repbot_schema.message_settings
    RENAME TO reputation_settings;

-- This function was decomissioned a long time ago but never removed
DROP FUNCTION IF EXISTS repbot_schema.get_thank_settings(_guild_id BIGINT);

-- Add the reputation mode to the guild settings to allow different ranking modes.
ALTER TABLE repbot_schema.guild_settings
    ADD IF NOT EXISTS reputation_mode TEXT DEFAULT 'TOTAL' NOT NULL;
