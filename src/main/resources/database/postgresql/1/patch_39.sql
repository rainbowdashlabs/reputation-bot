CREATE TABLE repbot_schema.profile (
    guild_id        BIGINT NOT NULL
        CONSTRAINT profile_pk
            PRIMARY KEY,
    nickname        TEXT,
    description     TEXT
);
