create table if not exists repbot_schema.voice_activity
(
    relation_key bigint                  not null,
    guild_id        bigint                  not null,
    user_id_1    bigint                  not null,
    user_id_2    bigint                  not null,
    seen         timestamp default now() not null,
    constraint voice_activity_pk
        primary key (relation_key, guild_id)
);

create unique index if not exists voice_activity_relation_key_uindex
    on repbot_schema.voice_activity (relation_key);

create index if not exists voice_activity_user_id_1_index
    on repbot_schema.voice_activity (user_id_1);

create index if not exists voice_activity_user_id_2_index
    on repbot_schema.voice_activity (user_id_2);

