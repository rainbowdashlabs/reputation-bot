CREATE TABLE repbot_schema.settings_audit_log (
    guild_id            BIGINT                  NOT NULL,
    member_id           BIGINT                  NOT NULL,
    settings_identifier TEXT                    NOT NULL,
    old_value           JSONB                   NOT NULL,
    new_value           JSONB                   NOT NULL,
    changed             TIMESTAMP DEFAULT now() NOT NULL
);

CREATE INDEX settings_audit_log_guild_id_member_id_index
    ON repbot_schema.settings_audit_log (guild_id, member_id);

CREATE INDEX settings_audit_log_guild_id_settings_identifier_index
    ON repbot_schema.settings_audit_log (guild_id, settings_identifier);

CREATE FUNCTION repbot_schema.audit_settings_changes(
) RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    IF old.value != new.value THEN
        new.changed = now();
    END IF;
    RETURN new;
END;
$$;

CREATE TRIGGER audit_settings_changed
    BEFORE UPDATE
    ON repbot_schema.settings_audit_log
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.audit_settings_changes();

CREATE FUNCTION repbot_schema.audit_settings_reverted(
) RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    IF old.value == new.value THEN
        DELETE FROM repbot_schema.settings_audit_log s WHERE s.settings_identifier = new.settings_identifier AND s.guild_id = new.guild_id AND s.member_id = new.member_id;
    END IF;
    RETURN new;
END;
$$;

CREATE TRIGGER audit_settings_reverted
    AFTER UPDATE
    ON repbot_schema.settings_audit_log
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.audit_settings_reverted();


CREATE TABLE repbot_schema.guild_session (
    guild_id  BIGINT                  NOT NULL,
    member_id BIGINT                  NOT NULL,
    token     TEXT                    NOT NULL
        CONSTRAINT guild_session_pk
            PRIMARY KEY,
    created   TIMESTAMP DEFAULT now() NOT NULL,
    last_used TIMESTAMP DEFAULT now() NOT NULL
);

CREATE INDEX guild_session_guild_id_member_id_token_index
    ON repbot_schema.guild_session (guild_id, member_id, token);

