ALTER TABLE repbot_schema.votes
    ADD COLUMN reminder BOOLEAN DEFAULT FALSE;

ALTER TABLE repbot_schema.votes
    ADD COLUMN reminder_timestamp TIMESTAMP DEFAULT now();

ALTER TABLE repbot_schema.votes
    ADD COLUMN sent BOOLEAN DEFAULT FALSE;
