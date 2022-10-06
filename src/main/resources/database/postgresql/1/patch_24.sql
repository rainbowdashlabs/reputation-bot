-- feature/static-metrics
-- Create a new table containing the reputation of each day grouped by day
CREATE TABLE repbot_schema.metrics_reputation
(
    day   DATE   NOT NULL,
    cause TEXT   NOT NULL,
    count BIGINT NOT NULL
);

CREATE INDEX metrics_reputation_day_index
    ON repbot_schema.metrics_reputation (day);

ALTER TABLE repbot_schema.metrics_reputation
    ADD CONSTRAINT metrics_reputation_pk
        PRIMARY KEY (day, cause);

-- prepopulate the table with data from the not current day. After this the metric service takes over.
INSERT INTO repbot_schema.metrics_reputation
SELECT received::DATE AS day,
       cause,
       COUNT(1)       AS count
FROM repbot_schema.reputation_log
WHERE received::DATE != NOW()::DATE
GROUP BY day, cause
ORDER BY day DESC;

-- We now need to recreate all views which were based on the reputation log itself
CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_type_week AS
SELECT DATE_TRUNC('week', day)::DATE AS week,
       cause,
       SUM(count)::BIGINT            AS count
FROM repbot_schema.metrics_reputation
GROUP BY week, cause
ORDER BY week DESC, cause;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_type_month AS
SELECT DATE_TRUNC('month', day)::DATE AS month,
       cause,
       SUM(count)::BIGINT             AS count
FROM repbot_schema.metrics_reputation
GROUP BY month, cause
ORDER BY month DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_dow_week AS
SELECT DATE_TRUNC('week', day)::DATE AS week,
       EXTRACT(ISODOW FROM day)      AS dow,
       SUM(count)::BIGINT            AS count
FROM repbot_schema.metrics_reputation
GROUP BY week, dow
ORDER BY week DESC, dow ASC;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_week AS
SELECT DATE_TRUNC('week', day)::DATE AS week,
       SUM(count)::BIGINT            AS count
FROM repbot_schema.metrics_reputation
GROUP BY week
ORDER BY week DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_month AS
SELECT DATE_TRUNC('month', day)::DATE AS month,
       SUM(count)::BIGINT             AS count
FROM repbot_schema.metrics_reputation
GROUP BY month
ORDER BY month DESC;

-- Create a table showing the total of saved reputation in the database
CREATE TABLE repbot_schema.metrics_reputation_count
(
    day     DATE              NOT NULL
        CONSTRAINT metrics_reputation_count_pk
            PRIMARY KEY,
    count   INTEGER DEFAULT 0 NOT NULL,
    added   INTEGER DEFAULT 0 NOT NULL,
    removed INTEGER DEFAULT 0 NOT NULL
);

-- Prepopulate counts
INSERT INTO repbot_schema.metrics_reputation_count
SELECT day,
       SUM(count) OVER (ORDER BY day) AS count
FROM (SELECT received::DATE AS day,
             COUNT(1)       AS count
      FROM repbot_schema.reputation_log
      GROUP BY day
      ORDER BY day DESC) m
WHERE day != NOW()::DATE
ORDER BY day DESC
ON CONFLICT(day) DO NOTHING;



CREATE OR REPLACE FUNCTION repbot_schema.reputation_added()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$BODY$
BEGIN
    INSERT INTO repbot_schema.metrics_reputation_count AS c (day, added)
    VALUES (NOW()::DATE, 1)
    ON CONFLICT(day) DO UPDATE SET added = c.added + 1;
    RETURN new;
END;
$BODY$;

CREATE OR REPLACE FUNCTION repbot_schema.reputation_removed()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$BODY$
BEGIN
    INSERT INTO repbot_schema.metrics_reputation_count AS c (day, removed)
    VALUES (NOW()::DATE, 1)
    ON CONFLICT(day) DO UPDATE SET removed = c.removed + 1;
    RETURN new;
END;
$BODY$;

-- Create triggers to count insert and deletions on reputation table.
CREATE TRIGGER reputation_add
    AFTER INSERT
    ON repbot_schema.reputation_log
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.reputation_added();

CREATE OR REPLACE TRIGGER reputation_remove
    AFTER DELETE
    ON repbot_schema.reputation_log
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.reputation_removed();

-- Create tables for user metrics
CREATE TABLE repbot_schema.metrics_users_week
(
    week           DATE   NOT NULL
        CONSTRAINT metrics_users_week_pk
            PRIMARY KEY,
    donor_count    BIGINT NOT NULL,
    receiver_count BIGINT NOT NULL,
    total_count    BIGINT NOT NULL
);

CREATE TABLE repbot_schema.metrics_users_month
(
    month          DATE   NOT NULL
        CONSTRAINT metrics_users_month_pk
            PRIMARY KEY,
    donor_count    BIGINT NOT NULL,
    receiver_count BIGINT NOT NULL,
    total_count    BIGINT NOT NULL
);

-- Prepopulate tables
INSERT INTO repbot_schema.metrics_users_month
SELECT month, receiver_count, donor_count, total_count
FROM repbot_schema.metrics_unique_users_month;

INSERT INTO repbot_schema.metrics_users_week
SELECT week, receiver_count, donor_count, total_count
FROM repbot_schema.metrics_unique_users_week;

-- Add views on metrics_reputation_count
CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_changed_week AS
SELECT DATE_TRUNC('week', day)::DATE AS week,
       MAX(count)::BIGINT             AS count,
       SUM(added)::BIGINT             AS added,
       MAX(removed)::BIGINT           AS removed
FROM repbot_schema.metrics_reputation_count
GROUP BY week
ORDER BY week DESC;

CREATE OR REPLACE VIEW repbot_schema.metrics_reputation_changed_month AS
SELECT DATE_TRUNC('month', day)::DATE AS month,
       MAX(count)::BIGINT             AS count,
       SUM(added)::BIGINT             AS added,
       MAX(removed)::BIGINT           AS removed
FROM repbot_schema.metrics_reputation_count
GROUP BY month
ORDER BY month DESC;
