WITH
    rep_offset
        AS (
        SELECT
            o.user_id,
            sum(o.amount) AS reputation
        FROM
            reputation_offset o
        WHERE o.added > :date_init
          AND o.guild_id = :guild_id
          AND ( added > :reset_date OR :reset_date::TIMESTAMP IS NULL )
        GROUP BY o.user_id
           ),
    full_log
        AS (
        SELECT
            receiver_id AS user_id,
            count(1)    AS reputation
        FROM
            reputation_log
        WHERE received > :date_init
          AND ( received > :reset_date OR :reset_date::TIMESTAMP IS NULL )
          AND guild_id = :guild_id
          AND receiver_id NOT IN (
            SELECT
                user_id
            FROM
                cleanup_schedule
            WHERE guild_id = :guild_id
                                 )
        GROUP BY receiver_id
           ),
    offset_reputation
        AS (
        SELECT
            coalesce(f.user_id, o.user_id)                        AS user_id,
            -- apply offset to the normal reputation.
            coalesce(f.reputation, 0) + coalesce(o.reputation, 0) AS reputation
        FROM
            full_log f
                FULL JOIN rep_offset o
                ON f.user_id = o.user_id
           )
SELECT
    rank() OVER (ORDER BY reputation DESC) AS rank,
    user_id,
    reputation::BIGINT                     AS reputation
FROM
    offset_reputation rank
OFFSET ? LIMIT ?;
