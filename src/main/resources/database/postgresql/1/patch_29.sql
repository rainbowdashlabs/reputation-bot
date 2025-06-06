CREATE TABLE repbot_schema.subscriptions (
    id      BIGINT NOT NULL,
    sku     BIGINT NOT NULL,
    type    TEXT   NOT NULL,
    ends_at TIMESTAMP,
    CONSTRAINT premium_pk
        PRIMARY KEY (id, sku)
);

CREATE TABLE repbot_schema.guild_locale_overrides (
    guild_id BIGINT NOT NULL,
    code     TEXT   NOT NULL,
    value    TEXT   NOT NULL,
    CONSTRAINT guild_locale_overrides_pk
        PRIMARY KEY (guild_id, code)
);

CREATE TABLE repbot_schema.autopost (
    guild_id         BIGINT                   NOT NULL
        CONSTRAINT autopost_pk
            PRIMARY KEY,
    active           BOOLEAN DEFAULT FALSE    NOT NULL,
    channel_id       BIGINT,
    message_id       BIGINT,
    refresh_type     TEXT    DEFAULT 'UPDATE' NOT NULL,
    refresh_interval TEXT    DEFAULT 'DAILY'  NOT NULL
);

ALTER TABLE repbot_schema.guild_settings
    ALTER COLUMN emoji_debug SET DEFAULT FALSE;

WITH
    merci AS (
        SELECT
            guild_id,
            'merci'
        FROM
            repbot_schema.thankwords
        WHERE thankword = '(?:re)?merci'
             ),
    remerci AS (
        SELECT
            guild_id,
            'remerci'
        FROM
            merci
             ),
    comb AS (
        SELECT *
        FROM
            merci
        UNION
        SELECT *
        FROM
            remerci
             )
INSERT
INTO
    repbot_schema.thankwords
SELECT *
FROM
    comb;

DELETE
FROM
    repbot_schema.thankwords
WHERE thankword ~* '[(){}*. ]'
   OR thankword ILIKE '%\\s%';

CREATE TABLE repbot_schema.log_channel (
    guild_id   BIGINT                NOT NULL
        CONSTRAINT log_channel_pk
            PRIMARY KEY,
    active     BOOLEAN DEFAULT FALSE NOT NULL,
    channel_id BIGINT
);

DROP VIEW IF EXISTS repbot_schema.global_user_reputation;
DROP VIEW IF EXISTS repbot_schema.user_reputation;
DROP VIEW IF EXISTS repbot_schema.user_reputation_7_days;
DROP VIEW IF EXISTS repbot_schema.user_reputation_30_days;
DROP VIEW IF EXISTS repbot_schema.user_reputation_week;
DROP VIEW IF EXISTS repbot_schema.user_reputation_month;
DROP VIEW IF EXISTS repbot_schema.truncated_reputation_log;
DROP VIEW IF EXISTS repbot_schema.truncated_reputation_offset;
