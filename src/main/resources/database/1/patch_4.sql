DROP MATERIALIZED VIEW IF EXISTS repbot_schema.data_statistics;

CREATE MATERIALIZED VIEW repbot_schema.data_statistics AS(
SELECT (SELECT COUNT(1) FROM repbot_schema.guild_settings) AS guilds,
       (SELECT COUNT(1) FROM repbot_schema.active_channel) AS channel,
       (SELECT COUNT(1) FROM repbot_schema.reputation_log) AS total_reputation,
       (SELECT COUNT(1)
        FROM repbot_schema.reputation_log
        WHERE received > NOW() - '1 DAY'::INTERVAL)        AS today_reputation,
       (SELECT COUNT(1)
        FROM repbot_schema.reputation_log
        WHERE received > NOW() - '1 WEEK'::INTERVAL)       AS weekly_reputation,
       (SELECT COUNT(1) / 4
        FROM repbot_schema.reputation_log
        WHERE received > NOW() - '4 WEEK'::INTERVAL)       AS weekly_avg_reputation );

REFRESH MATERIALIZED VIEW repbot_schema.data_statistics;
