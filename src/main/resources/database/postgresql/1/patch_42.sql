ALTER TABLE repbot_schema.guild_session
    ADD CONSTRAINT guild_session_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds
            ON DELETE CASCADE;

ALTER TABLE repbot_schema.settings_audit_log
    ADD CONSTRAINT settings_audit_log_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds
            ON DELETE CASCADE;

CREATE OR REPLACE FUNCTION repbot_schema.audit_settings_changes(
) RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    new.old_value = coalesce(new.old_value, 'null'::JSONB);
    new.new_value = coalesce(new.new_value, 'null'::JSONB);

    IF new.old_value != new.new_value THEN
        new.changed = now();
    END IF;

    RETURN new;
END;
$$;

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.guild_session
    FOR EACH ROW
EXECUTE PROCEDURE repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.settings_audit_log
    FOR EACH ROW
EXECUTE PROCEDURE repbot_schema.register_guild_id();
