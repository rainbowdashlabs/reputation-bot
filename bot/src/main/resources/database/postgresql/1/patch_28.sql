CREATE OR REPLACE VIEW repbot_schema.truncated_reputation_offset AS
SELECT l.guild_id, l.user_id, sum(l.amount)::BIGINT as amount
FROM repbot_schema.reputation_offset l
         LEFT JOIN repbot_schema.guild_settings s ON l.guild_id = s.guild_id
WHERE s.reset_date IS NULL
   OR l.added > s.reset_date
   OR s.reset_date > NOW()::DATE
GROUP BY l.guild_id, user_id;
