ALTER TABLE repbot_schema.subscriptions
    ADD COLUMN IF NOT EXISTS persistent BOOLEAN DEFAULT FALSE NOT NULL;

