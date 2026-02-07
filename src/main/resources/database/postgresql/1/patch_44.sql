CREATE TABLE repbot_schema.integration_bypass (
    guild_id        BIGINT             NOT NULL
        CONSTRAINT integration_bypass_guilds_guild_id_fk
            REFERENCES repbot_schema.guilds,
    integration_id  BIGINT             NOT NULL,
    allow_reactions BOOL DEFAULT FALSE NOT NULL,
    allow_answer    BOOL DEFAULT FALSE NOT NULL,
    allow_mention   BOOL DEFAULT FALSE NOT NULL,
    allow_fuzzy     BOOL DEFAULT FALSE NOT NULL,
    allow_direct    BOOL DEFAULT FALSE NOT NULL,
    ignore_cooldown BOOL DEFAULT FALSE NOT NULL,
    ignore_limit    BOOL DEFAULT FALSE NOT NULL,
    ignore_context  BOOL DEFAULT FALSE NOT NULL,
    CONSTRAINT integration_bypass_pk
        UNIQUE (guild_id, integration_id)
);

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.integration_bypass
    FOR EACH ROW
EXECUTE PROCEDURE repbot_schema.register_guild_id();
