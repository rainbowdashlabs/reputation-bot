-- add the missing unique constrain on table. requires deduplication.
CREATE TEMP TABLE temp_channel ON COMMIT DROP AS
    (SELECT DISTINCT guild_id, channel_id
     FROM repbot_schema.active_channel);

DELETE
FROM repbot_schema.active_channel;

CREATE UNIQUE INDEX IF NOT EXISTS active_channel_guild_id_channel_id_uindex
    ON repbot_schema.active_channel (guild_id, channel_id);

INSERT INTO repbot_schema.active_channel(guild_id, channel_id)
SELECT guild_id, channel_id
FROM temp_channel;
