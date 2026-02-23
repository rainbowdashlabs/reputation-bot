CREATE TABLE IF NOT EXISTS repbot_schema.votes (
    user_id      BIGINT                  NOT NULL,
    botlist      TEXT                    NOT NULL,
    last_vote    TIMESTAMP DEFAULT now() NOT NULL,
    streak       INTEGER   DEFAULT 1     NOT NULL,
    votes        INTEGER   DEFAULT 1,
    streak_start TIMESTAMP DEFAULT now() NOT NULL,
    streak_days  INTEGER GENERATED ALWAYS AS (extract(DAYS FROM ( last_vote - streak_start ))) STORED,
    CONSTRAINT votes_pk
        PRIMARY KEY (user_id, botlist)
);

CREATE TABLE IF NOT EXISTS repbot_schema.vote_token (
    entity_id   BIGINT NOT NULL,
    entity_type TEXT,
    token       INTEGER,
    total_token INTEGER,
    CONSTRAINT vote_tokens_pk
        PRIMARY KEY (entity_id, entity_type)
);

CREATE TABLE IF NOT EXISTS repbot_schema.user_settings (
    id         BIGINT NOT NULL
        CONSTRAINT user_settings_pk
            PRIMARY KEY,
    vote_guild BIGINT
);

CREATE TABLE IF NOT EXISTS repbot_schema.vote_log (
    user_id  BIGINT                  NOT NULL,
    guild_id BIGINT,
    tokens   INTEGER                 NOT NULL,
    reason   TEXT                    NOT NULL,
    created  TIMESTAMP DEFAULT now() NOT NULL
);

CREATE INDEX IF NOT EXISTS vote_log_user_id_index
    ON repbot_schema.vote_log (user_id DESC);

CREATE INDEX vote_log_guild_id_index
    ON repbot_schema.vote_log (guild_id);

CREATE TABLE IF NOT EXISTS repbot_schema.token_purchases (
    guild_id     BIGINT                  NOT NULL
        CONSTRAINT token_purchases_guilds_guild_id_fk
            REFERENCES repbot_schema.guilds,
    feature_id   INTEGER                 NOT NULL,
    expires      TIMESTAMP DEFAULT now() + INTERVAL '30 days',
    auto_renewal BOOL      DEFAULT FALSE NOT NULL,
    CONSTRAINT token_purchases_pk
        PRIMARY KEY (guild_id, feature_id)
);

ALTER TABLE repbot_schema.guild_settings
    ADD everyone_token_purchase BOOL DEFAULT TRUE NOT NULL;

CREATE TABLE repbot_schema.user_mails (
    user_id                BIGINT    NOT NULL,
    source                 TEXT      NOT NULL,
    mail_hash              TEXT      NOT NULL
        CONSTRAINT user_mails_pk
            PRIMARY KEY,
    mail_short             TEXT      NOT NULL,
    verified               BOOL      NOT NULL,
    verification_requested TIMESTAMP NOT NULL,
    verification_code      TEXT      NOT NULL
);

CREATE INDEX user_mails_user_id_index
    ON repbot_schema.user_mails (user_id);

CREATE INDEX user_mails_verification_code_index
    ON repbot_schema.user_mails (verification_code);

CREATE TABLE IF NOT EXISTS repbot_schema.kofi_purchase (
    id             SERIAL,
    mail_hash      TEXT   NOT NULL,
    key            TEXT   NOT NULL,
    sku_id         BIGINT NOT NULL,
    type           TEXT   NOT NULL,
    expires_at     TIMESTAMP,
    transaction_id TEXT   NOT NULL,
    guild_id       BIGINT
        CONSTRAINT kofi_purchase_pk
            PRIMARY KEY
);

CREATE INDEX IF NOT EXISTS kofi_purchase_mail_hash_index
    ON repbot_schema.kofi_purchase (mail_hash);
