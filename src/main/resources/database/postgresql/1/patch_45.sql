DROP TRIGGER IF EXISTS audit_settings_reverted ON repbot_schema.settings_audit_log;

CREATE TRIGGER audit_settings_reverted
    AFTER UPDATE OR INSERT
    ON repbot_schema.settings_audit_log
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.audit_settings_reverted();

CREATE OR REPLACE FUNCTION repbot_schema.audit_settings_reverted(
) RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    IF new.old_value = new.new_value THEN
        DELETE FROM repbot_schema.settings_audit_log s WHERE s.settings_identifier = new.settings_identifier AND s.guild_id = new.guild_id AND s.member_id = new.member_id;
    END IF;

    RETURN new;
END;
$$;

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
        new.changed_count = new.changed_count + 1;
    END IF;

    RETURN new;
END;
$$;

