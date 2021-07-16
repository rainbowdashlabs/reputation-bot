DROP FUNCTION IF EXISTS repbot_schema.get_guild_ranking(_guild_id BIGINT, _limit INTEGER, _offset INTEGER);
CREATE OR REPLACE FUNCTION repbot_schema.get_guild_ranking(_guild_id BIGINT, _limit INTEGER, _offset INTEGER)
    RETURNS TABLE
            (
                rank       BIGINT,
                user_id    BIGINT,
                reputation BIGINT
            )
    LANGUAGE plpgsql
    ROWS 100
    COST 100
AS
$BODY$
BEGIN

    RETURN QUERY
        SELECT rep.rank,
               rep.user_id,
               rep.reputation
        FROM repbot_schema.get_guild_ranking(_guild_id) rep
        LIMIT _limit OFFSET _offset;

END;
$BODY$;

DROP FUNCTION IF EXISTS repbot_schema.get_guild_ranking(_guild_id BIGINT);
CREATE OR REPLACE FUNCTION repbot_schema.get_guild_ranking(_guild_id BIGINT)
    RETURNS TABLE
            (
                rank       BIGINT,
                user_id    BIGINT,
                reputation BIGINT
            )
    LANGUAGE plpgsql
    ROWS 100
    COST 100
AS
$BODY$
BEGIN

    RETURN QUERY
        SELECT ROW_NUMBER() OVER (ORDER BY reputation DESC),
               rep.user_id,
               rep.reputation
        FROM (
                 SELECT receiver_id AS user_id,
                        COUNT(1)    AS reputation
                 FROM repbot_schema.reputation_log log
                 WHERE guild_id = _guild_id
                   AND NOT EXISTS(SELECT 1
                                  FROM repbot_schema.cleanup_schedule clean
                                  WHERE clean.guild_id = _guild_id
                                    AND clean.user_id = log.receiver_id)
             ) rep;

END;
$BODY$;

DROP VIEW IF EXISTS repbot_schema.user_reputation;
CREATE OR REPLACE VIEW repbot_schema.user_reputation(rank, guild_id, user_id, reputation, donated) AS
SELECT ROW_NUMBER() OVER (PARTITION BY rank.guild_id ORDER BY rank.reputation DESC) AS rank,
       rank.guild_id,
       rank.user_id,
       rank.reputation,
       rank.donated
FROM (
         SELECT guild_id,
                user_id,
                reputation,
                donated
         FROM (
                  SELECT COALESCE(don.guild_id, rep.guild_id)    AS guild_id,
                         COALESCE(rep.receiver_id, don.donor_id) AS user_id,
                         COALESCE(rep.reputation, 0::BIGINT)     AS reputation,
                         COALESCE(don.donated, 0::BIGINT)        AS donated
                  FROM (
                           SELECT r.guild_id # r.receiver_id AS key,
                                  r.guild_id,
                                  r.receiver_id,
                                  COUNT(1)                   AS reputation
                           FROM repbot_schema.reputation_log r
                           GROUP BY r.guild_id, r.receiver_id
                       ) rep
                           FULL JOIN (
                      SELECT r.guild_id # r.donor_id AS key,
                             r.guild_id,
                             r.donor_id,
                             COUNT(1)                AS donated
                      FROM repbot_schema.reputation_log r
                      GROUP BY r.guild_id, r.donor_id
                  ) don
                                     ON rep.key = don.key) log
         WHERE NOT EXISTS(SELECT 1
                          FROM repbot_schema.cleanup_schedule clean
                          WHERE clean.guild_id = log.guild_id
                            AND clean.user_id = log.user_id)
     ) rank;
