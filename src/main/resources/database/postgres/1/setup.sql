CREATE TABLE IF NOT EXISTS repbot_schema.thankwords
(
    guild_id  BIGINT NOT NULL,
    thankword VARCHAR,
    CONSTRAINT thankwords_pk
        UNIQUE (guild_id, thankword)
);

CREATE INDEX IF NOT EXISTS thankwords_guild_id_index
    ON repbot_schema.thankwords (guild_id);

CREATE TABLE IF NOT EXISTS repbot_schema.guild_bot_settings
(
    guild_id     BIGINT NOT NULL,
    prefix       VARCHAR,
    language     TEXT,
    manager_role BIGINT,
    CONSTRAINT prefix_pk
        PRIMARY KEY (guild_id)
);

CREATE TABLE IF NOT EXISTS repbot_schema.message_settings
(
    guild_id         BIGINT               NOT NULL,
    max_message_age  INTEGER DEFAULT 5    NOT NULL,
    reaction         TEXT,
    reactions_active BOOLEAN DEFAULT TRUE NOT NULL,
    answer_active    BOOLEAN DEFAULT TRUE NOT NULL,
    mention_active   BOOLEAN DEFAULT TRUE NOT NULL,
    fuzzy_active     BOOLEAN DEFAULT TRUE NOT NULL,
    cooldown         INTEGER DEFAULT 30   NOT NULL,
    CONSTRAINT message_settings_pk
        PRIMARY KEY (guild_id)
);

CREATE TABLE IF NOT EXISTS repbot_schema.reputation_log
(
    guild_id       BIGINT                             NOT NULL,
    donor_id       BIGINT                             NOT NULL,
    receiver_id    BIGINT                             NOT NULL,
    message_id     BIGINT                             NOT NULL,
    received       TIMESTAMP DEFAULT now(),
    ref_message_id BIGINT,
    channel_id     BIGINT    DEFAULT 0                NOT NULL,
    cause          TEXT      DEFAULT 'NO_MATCH'::TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS repuation_log_donated
    ON repbot_schema.reputation_log (guild_id, donor_id);

CREATE INDEX IF NOT EXISTS repuation_log_received
    ON repbot_schema.reputation_log (guild_id, receiver_id);

CREATE UNIQUE INDEX IF NOT EXISTS reputation_log_guild_id_donor_id_receiver_id_message_id_uindex
    ON repbot_schema.reputation_log (guild_id, donor_id, receiver_id, message_id);

CREATE INDEX IF NOT EXISTS reputation_log_received_guild_id_donor_id_receiver_id_index
    ON repbot_schema.reputation_log (received DESC, guild_id ASC, donor_id ASC, receiver_id ASC);

CREATE TABLE IF NOT EXISTS repbot_schema.active_channel
(
    guild_id   BIGINT NOT NULL,
    channel_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS repbot_schema.guild_ranks
(
    guild_id   BIGINT NOT NULL,
    role_id    BIGINT NOT NULL,
    reputation BIGINT NOT NULL,
    CONSTRAINT guild_ranks_pk
        PRIMARY KEY (guild_id, role_id)
);

CREATE INDEX IF NOT EXISTS guild_ranks_guild_id_index
    ON repbot_schema.guild_ranks (guild_id);

CREATE TABLE IF NOT EXISTS repbot_schema.cleanup_schedule
(
    task_id      BIGSERIAL NOT NULL,
    guild_id     BIGINT    NOT NULL,
    user_id      BIGINT,
    delete_after TIMESTAMP DEFAULT (now() + '14 days'::INTERVAL)
);

CREATE UNIQUE INDEX IF NOT EXISTS cleanup_schedule_guild_id_user_id_uindex
    ON repbot_schema.cleanup_schedule (guild_id, user_id);

CREATE OR REPLACE VIEW repbot_schema.guild_settings
            (guild_id, max_message_age, reaction, reactions_active, answer_active, mention_active, fuzzy_active, prefix,
             thankswords, active_channels, cooldown, manager_role)
AS
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
FROM repbot_schema.message_settings ms
         LEFT JOIN repbot_schema.guild_bot_settings gbs
                   ON ms.guild_id = gbs.guild_id
         LEFT JOIN (
    SELECT t_1.guild_id,
           array_agg(t_1.thankword) AS thankswords
    FROM repbot_schema.thankwords t_1
    GROUP BY t_1.guild_id
) t
                   ON ms.guild_id = t.guild_id
         LEFT JOIN (
    SELECT active_channel.guild_id,
           array_agg(active_channel.channel_id) AS active_channels
    FROM repbot_schema.active_channel
    GROUP BY active_channel.guild_id
) ac
                   ON ms.guild_id = ac.guild_id;

CREATE OR REPLACE VIEW repbot_schema.user_reputation(rank, guild_id, user_id, reputation, donated) AS
SELECT row_number() OVER (PARTITION BY rank.guild_id ORDER BY rank.reputation DESC) AS rank,
       rank.guild_id,
       rank.user_id,
       rank.reputation,
       rank.donated
FROM (
         SELECT coalesce(don.guild_id, rep.guild_id)    AS guild_id,
                coalesce(rep.receiver_id, don.donor_id) AS user_id,
                coalesce(rep.reputation, 0::BIGINT)     AS reputation,
                coalesce(don.donated, 0::BIGINT)        AS donated
         FROM (
                  SELECT r.guild_id # r.receiver_id AS key,
                         r.guild_id,
                         r.receiver_id,
                         count(1)                   AS reputation
                  FROM repbot_schema.reputation_log r
                  GROUP BY r.guild_id, r.receiver_id
              ) rep
                  FULL JOIN (
             SELECT r.guild_id # r.donor_id AS key,
                    r.guild_id,
                    r.donor_id,
                    count(1)                AS donated
             FROM repbot_schema.reputation_log r
             GROUP BY r.guild_id, r.donor_id
         ) don
                            ON rep.key = don.key
     ) rank;

CREATE OR REPLACE VIEW repbot_schema.global_user_reputation(rank, user_id, reputation, donated) AS
SELECT row_number() OVER ()            AS rank,
       user_reputation.user_id,
       sum(user_reputation.reputation) AS reputation,
       sum(user_reputation.donated)    AS donated
FROM repbot_schema.user_reputation
GROUP BY user_reputation.user_id;

