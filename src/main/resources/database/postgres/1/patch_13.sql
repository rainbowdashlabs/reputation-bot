CREATE TABLE IF NOT EXISTS repbot_schema.abuse_protection
(
    guild_id         BIGINT               NOT NULL
        CONSTRAINT abuse_protection_pk
            PRIMARY KEY,
    min_messages     INTEGER DEFAULT 10   NOT NULL,
    max_message_age  INTEGER DEFAULT 30   NOT NULL,
    receiver_context BOOLEAN DEFAULT TRUE NOT NULL,
    donor_context    BOOLEAN DEFAULT TRUE NOT NULL,
    cooldown         INTEGER DEFAULT 30   NOT NULL
);

INSERT INTO repbot_schema.abuse_protection(
    SELECT s.guild_id, s.min_messages, s.max_message_age, TRUE, TRUE, s.cooldown
    FROM repbot_schema.message_settings s
);

ALTER TABLE repbot_schema.message_settings
    DROP COLUMN IF EXISTS max_message_age;

ALTER TABLE repbot_schema.message_settings
    DROP COLUMN IF EXISTS cooldown;

ALTER TABLE repbot_schema.message_settings
    DROP COLUMN IF EXISTS min_messages;

ALTER TABLE repbot_schema.message_settings
    ADD IF NOT EXISTS embed_active bool DEFAULT TRUE NOT NULL;

ALTER TABLE repbot_schema.guild_bot_settings
    RENAME TO guild_settings;

CREATE TABLE repbot_schema.thank_settings
(
    guild_id          BIGINT               NOT NULL
        CONSTRAINT thank_settings_pk
            PRIMARY KEY,
    reaction          TEXT,
    channel_whitelist BOOLEAN DEFAULT TRUE NOT NULL
);

INSERT INTO repbot_schema.thank_settings(
    SELECT m.guild_id, m.reaction, COALESCE(s.channel_whitelist, TRUE)
    FROM repbot_schema.message_settings m
             LEFT JOIN repbot_schema.guild_settings s ON m.guild_id = s.guild_id
);

ALTER TABLE repbot_schema.message_settings
    DROP COLUMN reaction;

ALTER TABLE repbot_schema.guild_settings
    DROP COLUMN channel_whitelist;

CREATE OR REPLACE FUNCTION repbot_schema.get_thank_settings(_guild_id BIGINT)
    RETURNS TABLE
            (
                reaction          TEXT,
                reactions         TEXT[],
                thankswords       VARCHAR[],
                active_channels   BIGINT[],
                channel_whitelist BOOLEAN,
                receiver_roles    BIGINT[],
                donor_roles       BIGINT[]
            )
    ROWS 1
    COST 100
    LANGUAGE plpgsql
AS
$BODY$
BEGIN

    RETURN QUERY
        WITH thank_settings AS (
            SELECT s.reaction,
                   s.channel_whitelist
            FROM repbot_schema.thank_settings s
            WHERE s.guild_id = _guild_id
        ),
             thankwords AS (
                 SELECT ARRAY_AGG(t.thankword) AS thankswords
                 FROM repbot_schema.thankwords t
                 WHERE t.guild_id = _guild_id
             ),
             active_channel AS (
                 SELECT ARRAY_AGG(c.channel_id) AS active_channels
                 FROM repbot_schema.active_channel c
                 WHERE c.guild_id = _guild_id
             ),
             guild_reactions AS (
                 SELECT ARRAY_AGG(r.reaction) AS reactions
                 FROM repbot_schema.guild_reactions r
                 WHERE r.guild_id = _guild_id
             ),
             receiver_roles AS (
                 SELECT ARRAY_AGG(r.role_id) AS receiver_roles
                 FROM repbot_schema.receiver_roles r
                 WHERE r.guild_id = _guild_id
             ),
             donor_roles AS (
                 SELECT ARRAY_AGG(r.role_id) AS donor_roles
                 FROM repbot_schema.donor_roles r
                 WHERE r.guild_id = _guild_id
             )
        SELECT ts.reaction,
               r.reactions,
               t.thankswords,
               c.active_channels,
               ts.channel_whitelist,
               rec.receiver_roles,
               don.donor_roles
        FROM thank_settings ts,
             thankwords t,
             active_channel c,
             guild_reactions r,
             receiver_roles rec,
             donor_roles don;
END;
$BODY$;
