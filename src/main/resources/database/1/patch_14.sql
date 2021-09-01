drop index IF EXISTS repbot_schema.voice_activity_relation_key_uindex;

create index IF NOT EXISTS voice_activity_relation_key_index
    on repbot_schema.voice_activity(relation_key);
