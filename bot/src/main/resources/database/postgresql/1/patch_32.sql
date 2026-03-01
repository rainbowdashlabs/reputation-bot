ALTER TABLE repbot_schema.subscription_error
    ADD COLUMN IF NOT EXISTS notified BOOLEAN DEFAULT FALSE NOT NULL;
