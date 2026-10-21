CREATE TABLE repbot_schema.command_aliases (
    guild_id BIGINT NOT NULL
        CONSTRAINT command_aliases_guilds_guild_id_fk
            REFERENCES repbot_schema.guilds
            ON DELETE CASCADE,
    command  TEXT   NOT NULL,
    alias    TEXT   NOT NULL,
    CONSTRAINT command_aliases_pk
        PRIMARY KEY (guild_id, command),
    CONSTRAINT command_aliases_alias_uindex
        UNIQUE (guild_id, alias)
);
