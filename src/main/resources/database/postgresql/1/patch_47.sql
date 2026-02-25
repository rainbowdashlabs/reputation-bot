CREATE TABLE IF NOT EXISTS repbot_schema.votes (
    user_id      BIGINT                  NOT NULL,
    botlist      TEXT                    NOT NULL,
    last_vote    TIMESTAMP DEFAULT now() NOT NULL,
    streak       INTEGER   DEFAULT 1     NOT NULL,
    votes        INTEGER   DEFAULT 1,
    streak_start TIMESTAMP DEFAULT now() NOT NULL,
    streak_days  INTEGER GENERATED ALWAYS AS (extract(DAYS FROM ( last_vote - streak_start ))) STORED,
    CONSTRAINT votes_pk
        PRIMARY KEY (user_id, botlist)
);

CREATE TABLE IF NOT EXISTS repbot_schema.vote_token (
    entity_id   BIGINT NOT NULL,
    entity_type TEXT,
    token       INTEGER,
    total_token INTEGER,
    CONSTRAINT vote_tokens_pk
        PRIMARY KEY (entity_id, entity_type)
);

CREATE TABLE IF NOT EXISTS repbot_schema.user_settings (
    id         BIGINT NOT NULL
        CONSTRAINT user_settings_pk
            PRIMARY KEY,
    vote_guild BIGINT
);

CREATE TABLE IF NOT EXISTS repbot_schema.vote_log (
    user_id  BIGINT                  NOT NULL,
    guild_id BIGINT,
    tokens   INTEGER                 NOT NULL,
    reason   TEXT                    NOT NULL,
    created  TIMESTAMP DEFAULT now() NOT NULL
);

CREATE INDEX IF NOT EXISTS vote_log_user_id_index
    ON repbot_schema.vote_log (user_id DESC);

CREATE INDEX vote_log_guild_id_index
    ON repbot_schema.vote_log (guild_id);

CREATE TABLE IF NOT EXISTS repbot_schema.token_purchases (
    guild_id     BIGINT                  NOT NULL
        CONSTRAINT token_purchases_guilds_guild_id_fk
            REFERENCES repbot_schema.guilds,
    feature_id   INTEGER                 NOT NULL,
    expires      TIMESTAMP DEFAULT now() + INTERVAL '30 days',
    auto_renewal BOOL      DEFAULT FALSE NOT NULL,
    CONSTRAINT token_purchases_pk
        PRIMARY KEY (guild_id, feature_id)
);

ALTER TABLE repbot_schema.guild_settings
    ADD everyone_token_purchase BOOL DEFAULT TRUE NOT NULL;

CREATE TABLE repbot_schema.user_mails (
    user_id                BIGINT    NOT NULL,
    source                 TEXT      NOT NULL,
    mail_hash              TEXT      NOT NULL
        CONSTRAINT user_mails_pk
            PRIMARY KEY,
    mail_short             TEXT      NOT NULL,
    verified               BOOL      NOT NULL,
    verification_requested TIMESTAMP NOT NULL,
    verification_code      TEXT      NOT NULL
);

CREATE INDEX user_mails_user_id_index
    ON repbot_schema.user_mails (user_id);

CREATE INDEX user_mails_verification_code_index
    ON repbot_schema.user_mails (verification_code);

CREATE TABLE IF NOT EXISTS repbot_schema.kofi_purchase (
    id             SERIAL PRIMARY KEY,
    mail_hash      TEXT   NOT NULL,
    key            TEXT   NOT NULL,
    sku_id         BIGINT NOT NULL,
    type           TEXT   NOT NULL,
    expires_at     TIMESTAMP,
    transaction_id TEXT   NOT NULL,
    guild_id       BIGINT
);

CREATE UNIQUE INDEX kofi_purchase_id_indexu
    ON repbot_schema.kofi_purchase (id);

CREATE INDEX kofi_purchase_guild_id_index
    ON repbot_schema.kofi_purchase (guild_id);

CREATE INDEX IF NOT EXISTS kofi_purchase_mail_hash_index
    ON repbot_schema.kofi_purchase (mail_hash);

ALTER TABLE repbot_schema.subscriptions
    ADD source TEXT DEFAULT 'DISCORD' NOT NULL;

/*
  SELECT DISTINCT
    c.table_schema,
    c.table_name
FROM information_schema.columns AS c
WHERE c.table_schema = 'repbot_schema'
  AND (
    c.table_name  ILIKE '%user%'
            OR c.column_name ILIKE '%user%'
            OR c.column_name ILIKE '%member%'
            OR c.column_name ILIKE '%entity%'
    )
ORDER BY
    c.table_schema,
    c.table_name;

cleanup_schedule check
gdpr_log check
reputation_offset
support_threads no user data
kofi_purchase check
user_mails check
user_session technical. no user data
user_settings check
user_token technical, no user data
voice_activity check
vote_log check
votes check
settings_audit_log check
 */

-- Add new offset to user data
CREATE OR REPLACE FUNCTION repbot_schema.aggregate_user_data(_user_id BIGINT
)
    RETURNS TEXT
    LANGUAGE plpgsql
    COST 100
AS
$BODY$
DECLARE
    _result JSONB = '{}'::JSONB;
    _temp   JSONB;
BEGIN
    SELECT
        jsonb_agg(to_jsonb(l))
    FROM
        repbot_schema.reputation_log l
    WHERE l.receiver_id = _user_id
       OR l.donor_id = _user_id
    INTO _temp;

    _result = jsonb_set(_result, '{reputation_log}', coalesce(_temp, '[]'::JSONB));

    SELECT
        jsonb_agg(to_jsonb(o))
    FROM
        repbot_schema.reputation_offset o
    WHERE user_id = _user_id
    INTO _temp;

    _result = jsonb_set(_result, '{reputation_offset}', coalesce(_temp, '[]'::JSONB));

    SELECT
        jsonb_agg(to_jsonb(c)) AS voice
    FROM
        repbot_schema.voice_activity c
    WHERE user_id_1 = _user_id
       OR user_id_2 = _user_id
    INTO _temp;

    _result = jsonb_set(_result, '{voice_activity}', coalesce(_temp, '[]'::JSONB));

    SELECT
        jsonb_agg(to_jsonb(c)) AS cleanup
    FROM
        repbot_schema.cleanup_schedule c
    WHERE c.user_id = _user_id
    INTO _temp;

    _result = jsonb_set(_result, '{cleanup_tasks}', coalesce(_temp, '[]'::JSONB));

    SELECT
        to_jsonb(l) AS gdpr
    FROM
        repbot_schema.gdpr_log l
    WHERE l.user_id = _user_id
    into _temp;

    _result = jsonb_set(_result, '{gdpr_log}', coalesce(_temp, '{}'::JSONB));

    SELECT
        jsonb_agg(to_jsonb(m)) AS mails
    FROM
        repbot_schema.user_mails m
    WHERE m.user_id = _user_id
    INTO _temp;

    _result = jsonb_set(_result, '{user_mails}', coalesce(_temp, '[]'::JSONB));

    SELECT
        jsonb_agg(to_jsonb(p)) AS purchases
    FROM
        repbot_schema.user_mails um
            LEFT JOIN repbot_schema.kofi_purchase p
            ON um.mail_hash = p.mail_hash
    WHERE um.user_id = _user_id
    into _temp;

    _result = jsonb_set(_result, '{kofi_purchases}', coalesce(_temp, '[]'::JSONB));

    SELECT
        to_jsonb(s) AS settings
    FROM
        repbot_schema.user_settings s
    WHERE s.id = _user_id
    INTO _temp;

    _result = jsonb_set(_result, '{user_settings}', coalesce(_temp, '{}'::JSONB));

    SELECT
        jsonb_agg(to_jsonb(v)) AS votes_log
    FROM
        repbot_schema.vote_log v
    WHERE v.user_id = _user_id
    INTO _temp;

    _result = jsonb_set(_result, '{vote_log}', coalesce(_temp, '[]'::JSONB));

    SELECT
        jsonb_agg(to_jsonb(v)) AS votes
    FROM
        repbot_schema.votes v
    WHERE v.user_id = _user_id
    INTO _temp;

    _result = jsonb_set(_result, '{votes}', coalesce(_temp, '[]'::JSONB));

    SELECT
        to_jsonb(v) AS token_votes
    FROM
        repbot_schema.vote_token v
    WHERE v.entity_id = _user_id
    INTO _temp;

    _result = jsonb_set(_result, '{vote_token}', coalesce(_temp, '{}'::JSONB));

    SELECT
        jsonb_agg(to_jsonb(s)) AS audit_log
    FROM
        repbot_schema.settings_audit_log s
    WHERE member_id = _user_id
    INTO _temp;

    _result = jsonb_set(_result, '{audit_log}', coalesce(_temp, '[]'::JSONB));

    RETURN jsonb_pretty(_result);
END;
$BODY$;
