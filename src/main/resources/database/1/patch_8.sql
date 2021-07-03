CREATE TABLE repbot_schema.guild_reactions
(
    guild_id BIGINT NOT NULL,
    reaction TEXT   NOT NULL
);

CREATE INDEX guild_reactions_guild_id_index
    ON repbot_schema.guild_reactions (guild_id);

CREATE UNIQUE INDEX guild_reactions_guild_id_reaction_uindex
    ON repbot_schema.guild_reactions (guild_id, reaction);
