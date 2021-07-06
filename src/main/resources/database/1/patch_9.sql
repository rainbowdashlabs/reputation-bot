DROP VIEW repbot_schema.guild_settings;
CREATE OR REPLACE VIEW repbot_schema.guild_settings AS
SELECT ms.guild_id,
       ms.max_message_age,
       ms.reaction,
       r.reactions,
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
         LEFT JOIN repbot_schema.guild_bot_settings gbs ON ms.guild_id = gbs.guild_id
         LEFT JOIN (SELECT tw.guild_id,
                           ARRAY_AGG(tw.thankword) AS thankswords
                    FROM repbot_schema.thankwords tw
                    GROUP BY tw.guild_id) t ON ms.guild_id = t.guild_id
         LEFT JOIN (SELECT active_channel.guild_id,
                           ARRAY_AGG(active_channel.channel_id) AS active_channels
                    FROM repbot_schema.active_channel
                    GROUP BY active_channel.guild_id) ac ON ms.guild_id = ac.guild_id
         LEFT JOIN (SELECT tw.guild_id,
                           ARRAY_AGG(tw.reaction) AS reactions
                    FROM repbot_schema.guild_reactions tw
                    GROUP BY tw.guild_id) r ON ms.guild_id = r.guild_id;

DROP MATERIALIZED VIEW repbot_schema.data_statistics;
CREATE MATERIALIZED VIEW repbot_schema.data_statistics AS
SELECT (SELECT COUNT(1) AS count
        FROM repbot_schema.message_settings)                             AS guilds,
       (SELECT COUNT(1) AS count
        FROM repbot_schema.active_channel)                             AS channel,
       (SELECT COUNT(1) AS count
        FROM repbot_schema.reputation_log)                             AS total_reputation,
       (SELECT COUNT(1) AS count
        FROM repbot_schema.reputation_log
        WHERE reputation_log.received > (NOW() - '1 day'::INTERVAL))   AS today_reputation,
       (SELECT COUNT(1) AS count
        FROM repbot_schema.reputation_log
        WHERE reputation_log.received > (NOW() - '7 days'::INTERVAL))  AS weekly_reputation,
       (SELECT COUNT(1) / 4
        FROM repbot_schema.reputation_log
        WHERE reputation_log.received > (NOW() - '28 days'::INTERVAL)) AS weekly_avg_reputation;

REFRESH MATERIALIZED VIEW repbot_schema.data_statistics;
