CREATE TABLE repbot_schema.self_cleanup
(
    guild_id BIGINT NOT NULL
        CONSTRAINT self_cleanup_pk
            PRIMARY KEY
);
