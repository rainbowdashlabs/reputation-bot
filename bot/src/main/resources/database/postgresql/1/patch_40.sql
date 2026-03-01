CREATE TABLE repbot_schema.deny_donor_roles (
    guild_id BIGINT NOT NULL
        CONSTRAINT deny_donor_roles_guilds_guild_id_fk
            REFERENCES repbot_schema.guilds
            ON DELETE CASCADE,
    role_id  BIGINT NOT NULL
);

CREATE UNIQUE INDEX deny_donor_roles_guild_id_role_id_uindex
    ON repbot_schema.deny_donor_roles (guild_id, role_id);

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.deny_donor_roles
    FOR EACH ROW
EXECUTE PROCEDURE repbot_schema.register_guild_id();

CREATE TABLE repbot_schema.deny_receiver_roles (
    guild_id BIGINT NOT NULL
        CONSTRAINT deny_donor_roles_guilds_guild_id_fk
            REFERENCES repbot_schema.guilds
            ON DELETE CASCADE,
    role_id  BIGINT NOT NULL
);

CREATE UNIQUE INDEX deny_receiver_roles_guild_id_role_id_uindex
    ON repbot_schema.deny_receiver_roles (guild_id, role_id);

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.deny_receiver_roles
    FOR EACH ROW
EXECUTE PROCEDURE repbot_schema.register_guild_id();
