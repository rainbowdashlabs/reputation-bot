CREATE TABLE IF NOT EXISTS repbot_schema.scan_results (
    guild_id    BIGINT                  NOT NULL
        CONSTRAINT scan_results_pk
            PRIMARY KEY
        CONSTRAINT scan_results_guilds_guild_id_fk
            REFERENCES repbot_schema.guilds
            ON DELETE CASCADE,
    result      JSONB                   NOT NULL,
    started_at  TIMESTAMP DEFAULT now() NOT NULL,
    finished_at TIMESTAMP
);

CREATE TRIGGER register_guild_id
    BEFORE INSERT OR UPDATE
    ON repbot_schema.scan_results
    FOR EACH ROW
EXECUTE PROCEDURE repbot_schema.register_guild_id();
