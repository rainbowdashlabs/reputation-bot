drop index IF EXISTS repbot_schema.repuation_log_donated;

create index IF NOT EXISTS reputation_log_donated
    on repbot_schema.reputation_log (guild_id, received, donor_id) WHERE donor_id IS NOT NULL;

