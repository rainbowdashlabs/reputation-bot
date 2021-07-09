ALTER TABLE repbot_schema.guild_bot_settings
    ADD IF NOT EXISTS emoji_debug BOOLEAN DEFAULT TRUE NOT NULL;

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
       gbs.channel_whitelist,
       gbs.emoji_debug
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
