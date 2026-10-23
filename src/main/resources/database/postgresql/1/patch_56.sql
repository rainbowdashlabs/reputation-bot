-- The dashboard shows Delete and Repost for servers without autopost settings
ALTER TABLE repbot_schema.autopost
    ALTER COLUMN refresh_type SET DEFAULT 'DELETE_AND_REPOST';
