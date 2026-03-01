ALTER TABLE repbot_schema.guild_settings
    DROP COLUMN IF EXISTS manager_role;

ALTER TABLE repbot_schema.guild_settings
    DROP COLUMN IF EXISTS prefix;

UPDATE repbot_schema.thank_settings
SET reaction = '🏅'
WHERE reaction IS NULL;

ALTER TABLE repbot_schema.thank_settings
    ALTER COLUMN reaction SET NOT NULL;

ALTER TABLE repbot_schema.thank_settings
    ALTER COLUMN reaction SET DEFAULT '🏅';
