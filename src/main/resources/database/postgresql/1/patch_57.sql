ALTER TABLE repbot_schema.integration_bypass
    DROP CONSTRAINT integration_bypass_guilds_guild_id_fk;

ALTER TABLE repbot_schema.integration_bypass
    ADD CONSTRAINT integration_bypass_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;
