CREATE TABLE IF NOT EXISTS repbot_schema.user_token
(
    user_id       BIGINT PRIMARY KEY,
    access_token  TEXT      NOT NULL,
    refresh_token TEXT      NOT NULL,
    expiry        TIMESTAMP NOT NULL
);

DROP TABLE IF EXISTS repbot_schema.guild_session;

CREATE TABLE IF NOT EXISTS repbot_schema.user_session
(
    token     VARCHAR(64) PRIMARY KEY,
    user_id   BIGINT    NOT NULL,
    created   TIMESTAMP NOT NULL DEFAULT now(),
    last_used TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS user_session_user_id_index
    ON repbot_schema.user_session (user_id);
