CREATE TABLE IF NOT EXISTS repbot_schema.voice_activity
(
    relation_key BIGINT                  NOT NULL,
    guild_id     BIGINT                  NOT NULL,
    user_id_1    BIGINT                  NOT NULL,
    user_id_2    BIGINT                  NOT NULL,
    seen         TIMESTAMP DEFAULT now() NOT NULL,
    CONSTRAINT voice_activity_pk
        PRIMARY KEY (relation_key, guild_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS voice_activity_relation_key_uindex
    ON repbot_schema.voice_activity (relation_key);

CREATE INDEX IF NOT EXISTS voice_activity_user_id_1_index
    ON repbot_schema.voice_activity (user_id_1);

CREATE INDEX IF NOT EXISTS voice_activity_user_id_2_index
    ON repbot_schema.voice_activity (user_id_2);

ALTER TABLE repbot_schema.message_settings
    ADD IF NOT EXISTS min_messages INT DEFAULT 10 NOT NULL;

DROP VIEW IF EXISTS repbot_schema.guild_settings;

CREATE OR REPLACE VIEW repbot_schema.guild_settings
AS
SELECT
    ms.guild_id,
    ms.max_message_age,
    ms.reaction,
    ms.reactions_active,
    ms.answer_active,
    ms.mention_active,
    ms.fuzzy_active,
    ms.min_messages,
    gbs.prefix,
    t.thankswords,
    ac.active_channels,
    ms.cooldown,
    gbs.manager_role
FROM
    repbot_schema.message_settings ms
        LEFT JOIN repbot_schema.guild_bot_settings gbs
                  ON ms.guild_id = gbs.guild_id
        LEFT JOIN (
        SELECT
            t_1.guild_id,
            array_agg(t_1.thankword) AS thankswords
        FROM
            repbot_schema.thankwords t_1
        GROUP BY t_1.guild_id
    ) t
                  ON ms.guild_id = t.guild_id
        LEFT JOIN (
        SELECT
            active_channel.guild_id,
            array_agg(active_channel.channel_id) AS active_channels
        FROM
            repbot_schema.active_channel
        GROUP BY active_channel.guild_id
    ) ac
                  ON ms.guild_id = ac.guild_id;
