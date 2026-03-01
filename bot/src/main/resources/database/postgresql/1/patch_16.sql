CREATE OR REPLACE VIEW repbot_schema.user_reputation_week(rank, guild_id, user_id, reputation, donated) AS
SELECT ROW_NUMBER() OVER (PARTITION BY rank.guild_id ORDER BY rank.reputation DESC) AS rank,
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
                WHERE r.received > now() - INTERVAL '1 week'
                  GROUP BY r.guild_id, r.receiver_id) rep
                     FULL JOIN (SELECT r.guild_id # r.donor_id AS key,
                                       r.guild_id,
                                       r.donor_id,
                                       COUNT(1)                AS donated
                                FROM repbot_schema.reputation_log r
                                GROUP BY r.guild_id, r.donor_id) don ON rep.key = don.key) log
      WHERE NOT (EXISTS(SELECT 1
                        FROM repbot_schema.cleanup_schedule clean
                        WHERE clean.guild_id = log.guild_id
                          AND clean.user_id = log.user_id))) rank;

CREATE OR REPLACE VIEW repbot_schema.user_reputation_month(rank, guild_id, user_id, reputation, donated) AS
SELECT ROW_NUMBER() OVER (PARTITION BY rank.guild_id ORDER BY rank.reputation DESC) AS rank,
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
                  WHERE r.received > now() - INTERVAL '4 week'
                  GROUP BY r.guild_id, r.receiver_id) rep
                     FULL JOIN (SELECT r.guild_id # r.donor_id AS key,
                                       r.guild_id,
                                       r.donor_id,
                                       COUNT(1)                AS donated
                                FROM repbot_schema.reputation_log r
                                GROUP BY r.guild_id, r.donor_id) don ON rep.key = don.key) log
      WHERE NOT (EXISTS(SELECT 1
                        FROM repbot_schema.cleanup_schedule clean
                        WHERE clean.guild_id = log.guild_id
                          AND clean.user_id = log.user_id))) rank;
