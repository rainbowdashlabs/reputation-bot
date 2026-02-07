create table repbot_schema.integration_bypass
(
    guild_id        BIGINT             not null
        constraint integration_bypass_guilds_guild_id_fk
            references repbot_schema.guilds,
    integration_id  BIGINT             not null,
    allow_reactions bool default FALSE not null,
    allow_answer    bool default FALSE not null,
    allow_mention   bool default FALSE not null,
    allow_fuzzy     bool default FALSE not null,
    ignore_cooldown bool default FALSE not null,
    ignore_limit    bool default FALSE not null,
    constraint integration_bypass_pk
        unique (guild_id, integration_id)
);

