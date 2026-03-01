ALTER TABLE repbot_schema.subscriptions
    ADD COLUMN IF NOT EXISTS purchase_type TEXT DEFAULT '' NOT NULL;
