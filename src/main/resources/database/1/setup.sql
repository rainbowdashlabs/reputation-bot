create table if not exists repbot_schema.thankwords
(
    guild_id  bigint not null,
    thankword varchar,
    constraint thankwords_pk
        unique (guild_id, thankword)
);

create index if not exists thankwords_guild_id_index
    on repbot_schema.thankwords (guild_id);

create table if not exists repbot_schema.guild_bot_settings
(
    guild_id     bigint not null,
    prefix       varchar,
    language     text,
    manager_role bigint,
    constraint prefix_pk
        primary key (guild_id)
);

create table if not exists repbot_schema.message_settings
(
    guild_id         bigint               not null,
    max_message_age  integer default 5    not null,
    reaction         text,
    reactions_active boolean default true not null,
    answer_active    boolean default true not null,
    mention_active   boolean default true not null,
    fuzzy_active     boolean default true not null,
    cooldown         integer default 30   not null,
    constraint message_settings_pk
        primary key (guild_id)
);

create table if not exists repbot_schema.reputation_log
(
    guild_id       bigint                             not null,
    donor_id       bigint                             not null,
    receiver_id    bigint                             not null,
    message_id     bigint                             not null,
    received       timestamp default now(),
    ref_message_id bigint,
    channel_id     bigint    default 0                not null,
    cause          text      default 'NO_MATCH'::text not null
);

create index if not exists repuation_log_donated
    on repbot_schema.reputation_log (guild_id, donor_id);

create index if not exists repuation_log_received
    on repbot_schema.reputation_log (guild_id, receiver_id);

create unique index if not exists reputation_log_guild_id_donor_id_receiver_id_message_id_uindex
    on repbot_schema.reputation_log (guild_id, donor_id, receiver_id, message_id);

create index if not exists reputation_log_received_guild_id_donor_id_receiver_id_index
    on repbot_schema.reputation_log (received desc, guild_id asc, donor_id asc, receiver_id asc);

create table if not exists repbot_schema.active_channel
(
    guild_id   bigint not null,
    channel_id bigint not null
);

create table if not exists repbot_schema.guild_ranks
(
    guild_id   bigint not null,
    role_id    bigint not null,
    reputation bigint not null,
    constraint guild_ranks_pk
        primary key (guild_id, role_id)
);

create index if not exists guild_ranks_guild_id_index
    on repbot_schema.guild_ranks (guild_id);

create table if not exists repbot_schema.cleanup_schedule
(
    task_id      bigserial not null,
    guild_id     bigint    not null,
    user_id      bigint,
    delete_after timestamp default (now() + '14 days'::interval)
);

create unique index if not exists cleanup_schedule_guild_id_user_id_uindex
    on repbot_schema.cleanup_schedule (guild_id, user_id);

create or replace view repbot_schema.guild_settings
            (guild_id, max_message_age, reaction, reactions_active, answer_active, mention_active, fuzzy_active, prefix,
             thankswords, active_channels, cooldown, manager_role)
as
SELECT ms.guild_id,
       ms.max_message_age,
       ms.reaction,
       ms.reactions_active,
       ms.answer_active,
       ms.mention_active,
       ms.fuzzy_active,
       gbs.prefix,
       t.thankswords,
       ac.active_channels,
       ms.cooldown,
       gbs.manager_role
FROM repbot.message_settings ms
         LEFT JOIN repbot.guild_bot_settings gbs ON ms.guild_id = gbs.guild_id
         LEFT JOIN (SELECT t_1.guild_id,
                           array_agg(t_1.thankword) AS thankswords
                    FROM repbot.thankwords t_1
                    GROUP BY t_1.guild_id) t ON ms.guild_id = t.guild_id
         LEFT JOIN (SELECT active_channel.guild_id,
                           array_agg(active_channel.channel_id) AS active_channels
                    FROM repbot.active_channel
                    GROUP BY active_channel.guild_id) ac ON ms.guild_id = ac.guild_id;

create or replace view repbot_schema.user_reputation(rank, guild_id, user_id, reputation, donated) as
SELECT row_number() OVER (PARTITION BY rank.guild_id ORDER BY rank.reputation DESC) AS rank,
       rank.guild_id,
       rank.user_id,
       rank.reputation,
       rank.donated
FROM (SELECT COALESCE(don.guild_id, rep.guild_id)    AS guild_id,
             COALESCE(rep.receiver_id, don.donor_id) AS user_id,
             COALESCE(rep.reputation, 0::bigint)     AS reputation,
             COALESCE(don.donated, 0::bigint)        AS donated
      FROM (SELECT r.guild_id # r.receiver_id AS key,
                   r.guild_id,
                   r.receiver_id,
                   count(1)                   AS reputation
            FROM repbot.reputation_log r
            GROUP BY r.guild_id, r.receiver_id) rep
               FULL JOIN (SELECT r.guild_id # r.donor_id AS key,
                                 r.guild_id,
                                 r.donor_id,
                                 count(1)                AS donated
                          FROM repbot.reputation_log r
                          GROUP BY r.guild_id, r.donor_id) don ON rep.key = don.key) rank;

create or replace view repbot_schema.global_user_reputation(rank, user_id, reputation, donated) as
SELECT row_number() OVER ()            AS rank,
       user_reputation.user_id,
       sum(user_reputation.reputation) AS reputation,
       sum(user_reputation.donated)    AS donated
FROM repbot.user_reputation
GROUP BY user_reputation.user_id;

