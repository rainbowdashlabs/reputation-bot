ALTER TABLE repbot_schema.cleanup_schedule
    ALTER COLUMN guild_id DROP NOT NULL;

CREATE OR REPLACE FUNCTION repbot_schema.aggregate_user_data(user_id BIGINT)
    RETURNS JSONB
    LANGUAGE plpgsql
    COST 100
AS
$BODY$
DECLARE
    _result JSONB;
BEGIN
    WITH reputation AS (
        SELECT jsonb_agg(
                       jsonb_build_object(
                               'guild', guild_id,
                               'channel', channel_id,
                               'donor', CASE WHEN donor_id = user_id THEN user_id END,
                               'receiver', CASE WHEN receiver_id = user_id THEN user_id END,
                               'message', message_id,
                               'ref_message', ref_message_id,
                               'cause', cause,
                               'received', received::TEXT
                           )
                   ) AS rep
        FROM repbot_schema.reputation_log l
        WHERE l.receiver_id = user_id
           OR l.donor_id = user_id
    ),
         voice_activity AS (
             SELECT jsonb_agg(
                            jsonb_build_object(
                                    'guild', guild_id,
                                    'user_1', CASE WHEN user_id_1 = user_id THEN user_id END,
                                    'user_2', CASE WHEN user_id_2 = user_id THEN user_id END,
                                    'seen', seen::TEXT
                                )
                        ) AS voice
             FROM repbot_schema.voice_activity
         )
    SELECT jsonb_build_object(
                   'reputation', coalesce(rep, '[]'::JSONB),
                   'voice_activity', coalesce(voice, '[]'::JSONB)
               )
    FROM reputation,
         voice_activity
    INTO _result;
    RETURN _result;
END;
$BODY$;

CREATE TABLE IF NOT EXISTS repbot_schema.gdpr_log
(
    user_id   BIGINT                  NOT NULL,
    received  timestamp,
    tries     INTEGER   DEFAULT 0,
    requested TIMESTAMP DEFAULT now() NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS gdpr_log_user_id_uindex
    ON repbot_schema.gdpr_log (user_id);

ALTER TABLE repbot_schema.cleanup_schedule
    ALTER COLUMN guild_id SET DEFAULT 0;

ALTER TABLE repbot_schema.cleanup_schedule
    ALTER COLUMN user_id SET DEFAULT 0;
