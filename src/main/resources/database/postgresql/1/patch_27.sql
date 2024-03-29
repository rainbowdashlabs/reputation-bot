CREATE VIEW repbot_schema.truncated_reputation_log AS
SELECT l.guild_id, l.receiver_id, l.donor_id
FROM repbot_schema.reputation_log l
         LEFT JOIN repbot_schema.guild_settings s ON l.guild_id = s.guild_id
WHERE s.reset_date IS NULL
   OR l.received > s.reset_date
   OR s.reset_date > NOW()::DATE;

CREATE VIEW repbot_schema.truncated_reputation_offset AS
SELECT l.guild_id, l.user_id, l.amount
FROM repbot_schema.reputation_offset l
         LEFT JOIN repbot_schema.guild_settings s ON l.guild_id = s.guild_id
WHERE s.reset_date IS NULL
   OR l.added > s.reset_date
   OR s.reset_date > NOW()::DATE;

CREATE OR REPLACE VIEW repbot_schema.user_reputation
            (rank, rank_donated, guild_id, user_id, reputation, rep_offset, raw_reputation, donated) AS
    -- Prefilter reputation log based on the reset date.
WITH reputation AS (SELECT l.guild_id, l.receiver_id, l.donor_id
                    FROM repbot_schema.truncated_reputation_log l),
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
                      FULL JOIN repbot_schema.truncated_reputation_offset o ON f.guild_id = o.guild_id AND f.user_id = o.user_id)
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
