DROP MATERIALIZED VIEW IF EXISTS repbot_schema.data_statistics;
CREATE MATERIALIZED VIEW repbot_schema.data_statistics AS
    WITH
        latest_guild_activity AS (
            SELECT
                guild_id,
                max(received) AS latest
            FROM
                repbot_schema.reputation_log
            GROUP BY guild_id
                                 ),
        latest_channel_activity AS (
            SELECT
                channel_id,
                max(received) AS latest
            FROM
                repbot_schema.reputation_log
            GROUP BY channel_id
                                 )
        (
            SELECT
                (
                    SELECT
                        count(1)
                    FROM
                        repbot_schema.guilds
                ) AS guilds,
                (
                    SELECT
                        count(1)
                    FROM
                        latest_guild_activity
                    WHERE latest > now() - '90 DAYS'::INTERVAL
                ) AS active_guilds,
                (
                    SELECT
                        count(1)
                    FROM
                        latest_channel_activity
                    WHERE latest > now() - '90 DAYS'::INTERVAL
                ) AS active_channel,
                (
                    SELECT
                        count(1)
                    FROM
                        repbot_schema.active_channel
                ) AS channel,
                (
                    SELECT
                        sum(added)
                    FROM
                        repbot_schema.metrics_reputation_count
                ) AS total_reputation,
                (
                    SELECT
                        count(1)
                    FROM
                        repbot_schema.reputation_log
                    WHERE received > now() - '1 DAY'::INTERVAL
                ) AS today_reputation,
                (
                    SELECT
                        sum(added)
                    FROM
                        repbot_schema.metrics_reputation_count
                    WHERE day >= now() - '1 WEEK'::INTERVAL
                ) AS weekly_reputation,
                (
                    SELECT
                        sum(added) / 4
                    FROM
                        repbot_schema.metrics_reputation_count
                    WHERE day >= now() - '4 WEEK'::INTERVAL
                ) AS weekly_avg_reputation
        );

REFRESH MATERIALIZED VIEW repbot_schema.data_statistics;
