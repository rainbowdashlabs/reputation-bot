CREATE TABLE IF NOT EXISTS repbot_schema.active_categories
(
    guild_id    BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    CONSTRAINT active_categories_pk
        PRIMARY KEY (guild_id, category_id)
);

CREATE INDEX IF NOT EXISTS active_categories_guild_id_index
    ON repbot_schema.active_categories (guild_id);
