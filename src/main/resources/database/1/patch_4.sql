DROP materialized view IF EXISTS repbot_schema.data_statistics;

create materialized view repbot_schema.data_statistics as
(
SELECT (Select count(1) from repbot_schema.guild_settings) as guilds,
       (select count(1) from repbot_schema.active_channel) as channel,
       (select count(1) from repbot_schema.reputation_log) as total_reputation,
       (select count(1)
        from repbot_schema.reputation_log
        where received > now() - '1 DAY'::INTERVAL)        as today_reputation,
       (select count(1)
        from repbot_schema.reputation_log
        where received > now() - '1 WEEK'::INTERVAL)       as weekly_reputation,
       (select count(1) / 4
        from repbot_schema.reputation_log
        where received > now() - '4 WEEK'::INTERVAL)       as weekly_avg_reputation );

refresh materialized view repbot_schema.data_statistics;

SELECT guilds, channel, total_reputation, today_reputation, weekly_reputation, weekly_avg_reputation
from data_statistics;
