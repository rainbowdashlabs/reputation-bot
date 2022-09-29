CREATE OR REPLACE VIEW repbot_schema.guilds AS
SELECT guild_id FROM reputation_log
UNION DISTINCT
SELECT guild_id FROM abuse_protection
UNION DISTINCT
SELECT guild_id FROM active_categories
UNION DISTINCT
SELECT guild_id FROM active_channel
UNION DISTINCT
SELECT guild_id FROM announcements
UNION DISTINCT
SELECT guild_id FROM donor_roles
UNION DISTINCT
SELECT guild_id FROM guild_ranks
UNION DISTINCT
SELECT guild_id FROM guild_reactions
UNION DISTINCT
SELECT guild_id FROM guild_settings
UNION DISTINCT
SELECT guild_id FROM message_states
UNION DISTINCT
SELECT guild_id FROM receiver_roles
UNION DISTINCT
SELECT guild_id FROM donor_roles
UNION DISTINCT
SELECT guild_id FROM reputation_offset
UNION DISTINCT
SELECT guild_id FROM reputation_settings
UNION DISTINCT
SELECT guild_id FROM thank_settings
UNION DISTINCT
SELECT guild_id FROM thankwords
UNION DISTINCT
SELECT guild_id FROM voice_activity
ORDER BY guild_id;

UPDATE repbot_schema.cleanup_schedule SET user_id = 0 WHERE user_id IS NULL;
