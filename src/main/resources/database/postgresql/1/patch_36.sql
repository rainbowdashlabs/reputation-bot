ALTER TABLE IF EXISTS repbot_schema.guild_settings
    DROP COLUMN IF EXISTS emoji_debug;

ALTER TABLE repbot_schema.abuse_protection
    ADD COLUMN IF NOT EXISTS cooldown_direction TEXT DEFAULT 'BIDIRECTIONAL' NOT NULL;

