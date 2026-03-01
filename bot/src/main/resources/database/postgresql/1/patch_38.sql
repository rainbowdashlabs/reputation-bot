ALTER TABLE repbot_schema.reputation_settings
    ADD command_active BOOLEAN DEFAULT FALSE NOT NULL;

ALTER TABLE repbot_schema.message_states
    ADD command_reputation_ephemeral BOOLEAN DEFAULT FALSE NOT NULL;

