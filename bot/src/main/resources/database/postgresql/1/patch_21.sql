CREATE OR REPLACE FUNCTION repbot_schema.aggregate_user_data(_user_id BIGINT)
    RETURNS TEXT
    LANGUAGE plpgsql
    COST 100
AS
$BODY$
DECLARE
    _rep_given jsonb;
    _rep_received jsonb;
    _offset     jsonb;
    _voice      jsonb;
    _cleanup    jsonb;
    _gdpr       jsonb;
BEGIN
    SELECT JSONB_AGG(
                   JSONB_BUILD_OBJECT(
                           'guild', guild_id,
                           'channel', channel_id,
                           'donor', donor_id,
                           'message', message_id,
                           'ref_message', ref_message_id,
                           'cause', cause,
                           'received', received::TEXT
                       )
               ) AS rep
    FROM repbot_schema.reputation_log l
    WHERE l.donor_id = _user_id
    INTO _rep_given;

    SELECT JSONB_AGG(
                   JSONB_BUILD_OBJECT(
                           'guild', guild_id,
                           'channel', channel_id,
                           'receiver', receiver_id,
                           'message', message_id,
                           'ref_message', ref_message_id,
                           'cause', cause,
                           'received', received::TEXT
                       )
               ) AS rep
    FROM repbot_schema.reputation_log l
    WHERE l.receiver_id = _user_id
    INTO _rep_received;

    SELECT JSONB_AGG(
                   JSONB_BUILD_OBJECT(
                           'guild', guild_id,
                           'user', user_id,
                           'amount', amount
                       )
               ) AS rep_offset
    FROM repbot_schema.reputation_offset
    WHERE user_id = _user_id
    INTO _offset;

    SELECT JSONB_AGG(
                   JSONB_BUILD_OBJECT(
                           'guild', guild_id,
                           'user_1', CASE WHEN user_id_1 = _user_id THEN _user_id END,
                           'user_2', CASE WHEN user_id_2 = _user_id THEN _user_id END,
                           'seen', seen::TEXT
                       )
               ) AS voice
    FROM repbot_schema.voice_activity
    WHERE user_id_1 = _user_id
       OR user_id_2 = _user_id
    INTO _voice;

    SELECT JSONB_AGG(
                   JSONB_BUILD_OBJECT(
                           'guild', guild_id,
                           'user', user_id,
                           'delete_after', delete_after::TEXT
                       )
               ) AS cleanup
    FROM repbot_schema.cleanup_schedule c
    WHERE c.user_id = _user_id
    INTO _cleanup;

    SELECT JSONB_BUILD_OBJECT(
                   'user', user_id,
                   'received', NOW()::TEXT,
                   'attempts', attempts,
                   'requested', requested
               ) AS gdpr
    FROM repbot_schema.gdpr_log l
    WHERE l.user_id = _user_id
    INTO _gdpr;

    RETURN JSONB_PRETTY(
            JSONB_BUILD_OBJECT(
                    'reputation_given', COALESCE(_rep_received, '[]'::jsonb),
                    'reputation_received', COALESCE(_rep_received, '[]'::jsonb),
                    'voice_activity', COALESCE(_voice, '[]'::jsonb),
                    'cleanup_tasks', COALESCE(_cleanup, '[]'::jsonb),
                    'gdpr_log', COALESCE(_gdpr, '{}'::jsonb),
                    'reputation_offset', COALESCE(_offset, '[]'::jsonb)
                )
        );
END;
$BODY$;
