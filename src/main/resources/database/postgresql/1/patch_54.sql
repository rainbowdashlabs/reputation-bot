-- Allow announcing a given reputation per reputation type
ALTER TABLE repbot_schema.message_states
    ADD announce_reaction BOOLEAN DEFAULT TRUE NOT NULL;

ALTER TABLE repbot_schema.message_states
    ADD announce_answer BOOLEAN DEFAULT FALSE NOT NULL;

ALTER TABLE repbot_schema.message_states
    ADD announce_mention BOOLEAN DEFAULT FALSE NOT NULL;

ALTER TABLE repbot_schema.message_states
    ADD announce_fuzzy BOOLEAN DEFAULT FALSE NOT NULL;

ALTER TABLE repbot_schema.message_states
    ADD announce_embed BOOLEAN DEFAULT FALSE NOT NULL;

ALTER TABLE repbot_schema.message_states
    ADD announce_direct BOOLEAN DEFAULT FALSE NOT NULL;

ALTER TABLE repbot_schema.message_states
    ADD announce_command BOOLEAN DEFAULT FALSE NOT NULL;

-- Announcements are permanent unless this is enabled
ALTER TABLE repbot_schema.message_states
    ADD announce_delete BOOLEAN DEFAULT TRUE NOT NULL;

-- The reaction confirmation is replaced by the reputation announcement. Guilds which had it enabled
-- keep receiving a message on reaction reputation, and keep the delete after 30 seconds behaviour
-- that message had.
UPDATE repbot_schema.message_states
SET announce_reaction = reaction_confirmation,
    announce_delete   = reaction_confirmation;

ALTER TABLE repbot_schema.message_states
    DROP COLUMN reaction_confirmation;
