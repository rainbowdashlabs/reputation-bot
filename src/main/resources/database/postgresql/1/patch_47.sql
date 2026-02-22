CREATE TABLE IF NOT EXISTS repbot_schema.votes (
    user_id   BIGINT                  NOT NULL,
    botlist   TEXT                    NOT NULL,
    last_vote TIMESTAMP DEFAULT now() NOT NULL,
    streak    INTEGER   DEFAULT 0     NOT NULL,
    votes     INTEGER   DEFAULT 1     NOT NULL,
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
