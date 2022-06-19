CREATE TABLE IF NOT EXISTS repbot_schema.announcements
(
    guild_id     BIGINT
        CONSTRAINT announcements_pk
            PRIMARY KEY,
    active       BOOLEAN DEFAULT FALSE NOT NULL,
    same_channel BOOLEAN DEFAULT TRUE  NOT NULL,
    channel_id   BIGINT
);
