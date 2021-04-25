CREATE TABLE IF NOT EXISTS thankwords
(
    guild_id  bigint not null,
    thankword varchar,
    constraint thankwords_pk
        unique (guild_id, thankword)
);


CREATE INDEX IF NOT EXISTS thankwords_guild_id_index
    on thankwords (guild_id);

create table IF NOT EXISTS guild_bot_settings
(
    guild_id     bigint not null
        constraint prefix_pk
            primary key,
    prefix       varchar,
    language     text,
    manager_role bigint
);


CREATE TABLE IF NOT EXISTS message_settings
(
    guild_id         bigint               not null
        constraint message_settings_pk
            primary key,
    max_message_age  integer default 5    not null,
    reaction         text,
    reactions_active boolean default true not null,
    answer_active    boolean default true not null,
    mention_active   boolean default true not null,
    fuzzy_active     boolean default true not null,
    cooldown         integer default 30   not null
);


CREATE TABLE IF NOT EXISTS reputation_log
(
    guild_id    bigint not null,
    donor_id    bigint not null,
    receiver_id bigint not null,
    message_id  bigint not null,
    received    timestamp default now()
);

CREATE INDEX IF NOT EXISTS repuation_log_donated
    on reputation_log (guild_id, donor_id);

CREATE INDEX IF NOT EXISTS repuation_log_received
    on reputation_log (guild_id, receiver_id);

CREATE UNIQUE INDEX IF NOT EXISTS reputation_log_guild_id_donor_id_receiver_id_message_id_uindex
    on reputation_log (guild_id, donor_id, receiver_id, message_id);

CREATE TABLE IF NOT EXISTS active_channel
(
    guild_id   bigint not null,
    channel_id bigint not null
);


CREATE TABLE IF NOT EXISTS guild_ranks
(
    guild_id   bigint not null,
    role_id    bigint not null,
    reputation bigint not null,
    constraint guild_ranks_pk
        primary key (guild_id, role_id)
);

CREATE INDEX IF NOT EXISTS guild_ranks_guild_id_index
    on guild_ranks (guild_id);

create or replace view guild_settings
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
FROM message_settings ms
         LEFT JOIN guild_bot_settings gbs ON ms.guild_id = gbs.guild_id
         LEFT JOIN (SELECT t_1.guild_id,
                           array_agg(t_1.thankword) AS thankswords
                    FROM thankwords t_1
                    GROUP BY t_1.guild_id) t ON ms.guild_id = t.guild_id
         LEFT JOIN (SELECT active_channel.guild_id,
                           array_agg(active_channel.channel_id) AS active_channels
                    FROM active_channel
                    GROUP BY active_channel.guild_id) ac ON ms.guild_id = ac.guild_id;

create or replace view user_reputation(guild_id, user_id, reputation, donated) as
SELECT rep.guild_id,
       rep.receiver_id                     AS user_id,
       COALESCE(rep.reputation, 0::bigint) AS reputation,
       COALESCE(don.donated, 0::bigint)    AS donated
FROM (SELECT reputation_log.guild_id,
             reputation_log.receiver_id,
             count(1) AS reputation
      FROM repbot.reputation_log
      GROUP BY reputation_log.guild_id, reputation_log.receiver_id) rep
         LEFT JOIN (SELECT reputation_log.guild_id,
                           reputation_log.donor_id,
                           count(1) AS donated
                    FROM repbot.reputation_log
                    GROUP BY reputation_log.guild_id, reputation_log.donor_id) don ON rep.receiver_id = don.donor_id;