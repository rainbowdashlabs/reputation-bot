CREATE TABLE IF NOT EXISTS repbot_schema.self_cleanup
(
    guild_id BIGINT                  NOT NULL
        CONSTRAINT self_cleanup_pk
            PRIMARY KEY,
    prompted TIMESTAMP DEFAULT NOW() NOT NULL
);


CREATE TABLE IF NOT EXISTS repbot_schema.migrations
(
    guild_id BIGINT
        CONSTRAINT migrations_pk
            PRIMARY KEY,
    prompted TIMESTAMP DEFAULT NOW() NOT NULL,
    migrated TIMESTAMP
);
