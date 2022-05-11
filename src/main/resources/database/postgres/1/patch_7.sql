ALTER TABLE repbot_schema.guild_bot_settings
    ADD IF NOT EXISTS channel_whitelist BOOLEAN DEFAULT TRUE NOT NULL;

DROP VIEW repbot_schema.guild_settings CASCADE;
CREATE OR REPLACE VIEW repbot_schema.guild_settings
AS
SELECT ms.guild_id,
       ms.max_message_age,
       ms.reaction,
       ms.reactions_active,
       ms.answer_active,
       ms.mention_active,
       ms.fuzzy_active,
       ms.min_messages,
       gbs.prefix,
       gbs.language,
       t.thankswords,
       ac.active_channels,
       ms.cooldown,
       gbs.manager_role,
       gbs.channel_whitelist
FROM repbot_schema.message_settings ms
         LEFT JOIN repbot_schema.guild_bot_settings gbs
                   ON ms.guild_id = gbs.guild_id
         LEFT JOIN (
    SELECT tw.guild_id,
           ARRAY_AGG(tw.thankword) AS thankswords
    FROM repbot_schema.thankwords tw
    GROUP BY tw.guild_id
) t
                   ON ms.guild_id = t.guild_id
         LEFT JOIN (
    SELECT active_channel.guild_id,
           ARRAY_AGG(active_channel.channel_id) AS active_channels
    FROM repbot_schema.active_channel
    GROUP BY active_channel.guild_id
) ac
                   ON ms.guild_id = ac.guild_id;

DROP MATERIALIZED VIEW IF EXISTS repbot_schema.data_statistics;
CREATE MATERIALIZED VIEW repbot_schema.data_statistics AS
(
SELECT (SELECT COUNT(1) FROM repbot_schema.guild_settings) AS guilds,
       (SELECT COUNT(1) FROM repbot_schema.active_channel) AS channel,
       (SELECT COUNT(1) FROM repbot_schema.reputation_log) AS total_reputation,
       (SELECT COUNT(1)
        FROM repbot_schema.reputation_log
        WHERE received > NOW() - '1 DAY'::INTERVAL)        AS today_reputation,
       (SELECT COUNT(1)
        FROM repbot_schema.reputation_log
        WHERE received > NOW() - '1 WEEK'::INTERVAL)       AS weekly_reputation,
       (SELECT COUNT(1) / 4
        FROM repbot_schema.reputation_log
        WHERE received > NOW() - '4 WEEK'::INTERVAL)       AS weekly_avg_reputation );

REFRESH MATERIALIZED VIEW repbot_schema.data_statistics;
