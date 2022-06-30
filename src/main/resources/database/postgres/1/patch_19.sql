CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_dow_week AS
SELECT DATE_TRUNC('week', received)::DATE AS week, EXTRACT(ISODOW FROM received) AS dow, COUNT(1) AS count
FROM repbot_schema.reputation_log
GROUP BY week, dow
ORDER BY week DESC, dow ASC;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_dow_month AS
SELECT DATE_TRUNC('month', week)::DATE AS month, dow, AVG(count)::INTEGER AS count
FROM repbot_schema.metrics_reputation_dow_week
GROUP BY month, dow
ORDER BY month DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_dow_year AS
SELECT DATE_TRUNC('year', week)::DATE AS year, dow, AVG(w.count)::INTEGER AS count
FROM repbot_schema.metrics_reputation_dow_week w
GROUP BY year, dow
ORDER BY year DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_week AS
SELECT DATE_TRUNC('week', received)::DATE AS week, COUNT(1) AS count
FROM repbot_schema.reputation_log
GROUP BY week
ORDER BY week DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_month AS
SELECT DATE_TRUNC('month', received)::DATE AS month, COUNT(1) AS count
FROM repbot_schema.reputation_log
GROUP BY month
ORDER BY month DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_total_week AS
SELECT week, SUM(count) OVER (ORDER BY week) AS count
FROM repbot_schema.metrics_reputation_week m
ORDER BY week DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_total_month AS
SELECT month, SUM(count) OVER (ORDER BY month) AS count
FROM repbot_schema.metrics_reputation_month m
ORDER BY month DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_messages_total_day AS
SELECT day, SUM(count) OVER (ORDER BY day) AS count
FROM repbot_schema.metrics_message_analyzed_day m
ORDER BY day DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_messages_total_week AS
SELECT week, SUM(count) OVER (ORDER BY week) AS count
FROM repbot_schema.metrics_message_analyzed_week m
ORDER BY week DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_messages_total_month AS
SELECT month, SUM(count) OVER (ORDER BY month) AS count
FROM repbot_schema.metrics_message_analyzed_month m
ORDER BY month DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_commands_executed_week AS
SELECT DATE_TRUNC('week', day)::DATE AS week, SUM(count) AS count
FROM repbot_schema.metrics_commands
GROUP BY week
ORDER BY week DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_commands_executed_month AS
SELECT DATE_TRUNC('month', day)::DATE AS month, SUM(count) AS count
FROM repbot_schema.metrics_commands
GROUP BY month
ORDER BY month DESC;

CREATE FUNCTION repbot_schema.snowflake_to_unix_timestamp(snowflake BIGINT) RETURNS TIMESTAMP
    LANGUAGE plpgsql
    PARALLEL SAFE
    IMMUTABLE
AS
$BODY$
BEGIN
    -- message_id::BIT(64) AS bits
    -- bits::BIT(42) AS timestamp_bits
    -- timestamp_bits::BIGINT AS discord_epoch
    -- discord_epoch + 1420070400000 AS unix
    -- to_timestamp(unix / 1000.0) as timestamp
    RETURN TO_TIMESTAMP((snowflake::BIT(64)::BIT(42)::BIGINT + 1420070400000) / 1000.0);
END;
$BODY$;

UPDATE repbot_schema.reputation_log
SET received = repbot_schema.snowflake_to_unix_timestamp(message_id)
WHERE received - repbot_schema.snowflake_to_unix_timestamp(message_id) > (INTERVAL '1 day');
