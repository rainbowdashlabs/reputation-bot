CREATE TABLE IF NOT EXISTS repbot_schema.receiver_roles
(
    guild_id BIGINT NOT NULL,
    role_id  BIGINT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS receiver_roles_guild_id_role_id_uindex
    ON repbot_schema.receiver_roles (guild_id, role_id);

CREATE TABLE IF NOT EXISTS repbot_schema.donor_roles
(
    guild_id BIGINT NOT NULL,
    role_id  BIGINT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS donor_roles_guild_id_role_id_uindex
    ON repbot_schema.donor_roles (guild_id, role_id);

DROP VIEW repbot_schema.guild_settings;

DROP FUNCTION repbot_schema.get_guild_settings(_guild_id BIGINT);
CREATE OR REPLACE FUNCTION repbot_schema.get_guild_settings(_guild_id BIGINT)
    RETURNS TABLE
            (
                guild_id          BIGINT,
                max_message_age   INT,
                reaction          TEXT,
                reactions         TEXT[],
                reactions_active  BOOLEAN,
                answer_active     BOOLEAN,
                mention_active    BOOLEAN,
                fuzzy_active      BOOLEAN,
                min_messages      INTEGER,
                prefix            VARCHAR,
                language          TEXT,
                thankswords       VARCHAR[],
                active_channels   BIGINT[],
                cooldown          INTEGER,
                manager_role      BIGINT,
                channel_whitelist BOOLEAN,
                receiver_roles    BIGINT[],
                donor_roles       BIGINT[],
                emoji_debug       BOOLEAN
            )
    ROWS 1
    COST 100
    LANGUAGE plpgsql
AS
$BODY$
BEGIN

    RETURN QUERY
        WITH message_settings AS (
            SELECT s.max_message_age,
                   s.reaction,
                   s.reactions_active,
                   s.answer_active,
                   s.mention_active,
                   s.fuzzy_active,
                   s.cooldown,
                   s.min_messages
            FROM repbot_schema.message_settings s
            WHERE s.guild_id = _guild_id
        ),
             bot_settings AS (
                 SELECT s.prefix,
                        s.language,
                        s.manager_role,
                        s.channel_whitelist,
                        s.emoji_debug
                 FROM repbot_schema.guild_bot_settings s
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
        SELECT _guild_id,
               mes.max_message_age,
               mes.reaction,
               r.reactions,
               mes.reactions_active,
               mes.answer_active,
               mes.mention_active,
               mes.fuzzy_active,
               mes.min_messages,
               bot.prefix,
               bot.language,
               t.thankswords,
               c.active_channels,
               mes.cooldown,
               bot.manager_role,
               bot.channel_whitelist,
               rec.receiver_roles,
               don.donor_roles,
               bot.emoji_debug
        FROM message_settings mes,
             bot_settings bot,
             thankwords t,
             active_channel c,
             guild_reactions r,
             receiver_roles rec,
             donor_roles don;
END;
$BODY$;

SELECT *
FROM get_guild_settings(853250161915985958)
