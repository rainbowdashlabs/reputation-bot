-- feature/guild-cleanup
CREATE OR REPLACE VIEW repbot_schema.guilds AS
SELECT guild_id FROM repbot_schema.reputation_log
UNION DISTINCT
SELECT guild_id FROM repbot_schema.abuse_protection
UNION DISTINCT
SELECT guild_id FROM repbot_schema.active_categories
UNION DISTINCT
SELECT guild_id FROM repbot_schema.active_channel
UNION DISTINCT
SELECT guild_id FROM repbot_schema.announcements
UNION DISTINCT
SELECT guild_id FROM repbot_schema.donor_roles
UNION DISTINCT
SELECT guild_id FROM repbot_schema.guild_ranks
UNION DISTINCT
SELECT guild_id FROM repbot_schema.guild_reactions
UNION DISTINCT
SELECT guild_id FROM repbot_schema.guild_settings
UNION DISTINCT
SELECT guild_id FROM repbot_schema.message_states
UNION DISTINCT
SELECT guild_id FROM repbot_schema.receiver_roles
UNION DISTINCT
SELECT guild_id FROM repbot_schema.donor_roles
UNION DISTINCT
SELECT guild_id FROM repbot_schema.reputation_offset
UNION DISTINCT
SELECT guild_id FROM repbot_schema.reputation_settings
UNION DISTINCT
SELECT guild_id FROM repbot_schema.thank_settings
UNION DISTINCT
SELECT guild_id FROM repbot_schema.thankwords
UNION DISTINCT
SELECT guild_id FROM repbot_schema.voice_activity
ORDER BY guild_id;

UPDATE repbot_schema.cleanup_schedule SET user_id = 0 WHERE user_id IS NULL;

-- feature/analyzer-log
CREATE TABLE repbot_schema.analyzer_results
(
    guild_id   BIGINT NOT NULL,
    channel_id BIGINT NOT NULL,
    message_id BIGINT NOT NULL,
    result     jsonb  NOT NULL,
    analyzed   TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc') NOT NULL,
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
