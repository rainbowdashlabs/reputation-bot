CREATE TABLE IF NOT EXISTS repbot_schema.voice_activity
(
    relation_key BIGINT                  NOT NULL,
    guild_id     BIGINT                  NOT NULL,
    user_id_1    BIGINT                  NOT NULL,
    user_id_2    BIGINT                  NOT NULL,
    seen         TIMESTAMP DEFAULT now() NOT NULL,
    CONSTRAINT voice_activity_pk
        PRIMARY KEY (relation_key, guild_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS voice_activity_relation_key_uindex
    ON repbot_schema.voice_activity (relation_key);

CREATE INDEX IF NOT EXISTS voice_activity_user_id_1_index
    ON repbot_schema.voice_activity (user_id_1);

CREATE INDEX IF NOT EXISTS voice_activity_user_id_2_index
    ON repbot_schema.voice_activity (user_id_2);

