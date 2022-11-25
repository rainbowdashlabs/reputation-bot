CREATE TABLE repbot_schema.reputation_results
(
    guild_id   BIGINT                                       NOT NULL,
    channel_id BIGINT                                       NOT NULL,
    message_id BIGINT                                       NOT NULL,
    result     jsonb                                        NOT NULL,
    submitted  TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'utc') NOT NULL
);

CREATE INDEX reputation_results_guild_id_message_id_index
    ON repbot_schema.reputation_results (guild_id, message_id);
