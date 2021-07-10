CREATE OR REPLACE FUNCTION repbot_schema.get_guild_stats(_guild_id BIGINT)
    RETURNS TABLE
            (
                total_reputation BIGINT,
                week_reputation  BIGINT,
                today_reputation BIGINT,
                top_channel      BIGINT
            )
    LANGUAGE plpgsql
    ROWS 1
    COST 100
AS
$BODY$
BEGIN
    RETURN QUERY
        SELECT (SELECT COUNT(1)
                FROM repbot_schema.reputation_log
                WHERE guild_id = _guild_id) AS total_repuation,
               (SELECT COUNT(1)
                FROM repbot_schema.reputation_log
                WHERE received > (NOW() - '7 days'::INTERVAL)
                  AND guild_id = _guild_id) AS week_reputation,
               (SELECT COUNT(1) AS count
                FROM repbot_schema.reputation_log
                WHERE received > (NOW() - '1 day'::INTERVAL)
                    AND guild_id = _guild_id),
               (SELECT channel_id
                FROM (SELECT COUNT(1) AS count, channel_id
                      FROM repbot_schema.reputation_log
                      WHERE guild_id = _guild_id
                      GROUP BY channel_id
                      ORDER BY count
                      LIMIT 1) counts) as top_channel;
END;
$BODY$;
