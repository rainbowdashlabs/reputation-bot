CREATE TABLE repbot_schema.guilds_archived (
    guild_id        BIGINT    NOT NULL,
    date_joined     TIMESTAMP NOT NULL,
    latest_activity TIMESTAMP NOT NULL,
    date_left       TIMESTAMP NOT NULL
);

ALTER TABLE repbot_schema.guilds
    ADD date_joined TIMESTAMP DEFAULT now() NOT NULL;

ALTER TABLE repbot_schema.guilds
    ADD latest_activity TIMESTAMP DEFAULT now() NOT NULL;

ALTER TABLE repbot_schema.guilds
    ADD date_left TIMESTAMP;

CREATE OR REPLACE FUNCTION repbot_schema.register_guild_id(
)
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$BODY$
BEGIN
    INSERT
    INTO
        repbot_schema.guilds
    VALUES
        (new.guild_id)
    ON CONFLICT (guild_id) DO UPDATE SET latest_activity = now();
    RETURN new;
END;
$BODY$;

UPDATE repbot_schema.guilds
SET
    date_joined     = repbot_schema.snowflake_to_unix_timestamp(guild_id),
    latest_activity = repbot_schema.snowflake_to_unix_timestamp(guild_id);
