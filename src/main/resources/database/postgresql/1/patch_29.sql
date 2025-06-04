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
