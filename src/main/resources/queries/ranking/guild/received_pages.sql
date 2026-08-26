-- Counts the pages of ranking/guild/received.sql. Both sides of the full join there are grouped by
-- user, so the join yields one row per user present in either table and this union counts the same
-- set. Any change to the population in received.sql belongs here as well.
SELECT
    ceil(count(1)::NUMERIC / :page_size) AS count
FROM
    (
        SELECT
            receiver_id AS user_id
        FROM
            reputation_log
        WHERE guild_id = :guild_id
          AND received > :date_init
          AND ( received > :reset_date OR :reset_date::TIMESTAMP IS NULL )
          AND receiver_id NOT IN (
            SELECT
                user_id
            FROM
                cleanup_schedule
            WHERE guild_id = :guild_id
                                 )
        UNION
        SELECT
            user_id
        FROM
            reputation_offset
        WHERE guild_id = :guild_id
          AND added > :date_init
          AND ( added > :reset_date OR :reset_date::TIMESTAMP IS NULL )
          AND user_id NOT IN (
            SELECT
                user_id
            FROM
                cleanup_schedule
            WHERE guild_id = :guild_id
                             )
    ) a;
