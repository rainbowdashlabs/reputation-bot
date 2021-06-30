ALTER TABLE repbot_schema.gdpr_log
    RENAME COLUMN tries TO attempts;

DROP FUNCTION repbot_schema.aggregate_user_data(bigint);

CREATE OR REPLACE FUNCTION repbot_schema.aggregate_user_data(_user_id BIGINT)
    RETURNS TEXT
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
                               'donor', CASE WHEN donor_id = _user_id THEN _user_id END,
                               'receiver', CASE WHEN receiver_id = _user_id THEN _user_id END,
                               'message', message_id,
                               'ref_message', ref_message_id,
                               'cause', cause,
                               'received', received::TEXT
                           )
                   ) AS rep
        FROM repbot_schema.reputation_log l
        WHERE l.receiver_id = _user_id
           OR l.donor_id = _user_id
    ),
         voice_activity AS (
             SELECT jsonb_agg(
                            jsonb_build_object(
                                    'guild', guild_id,
                                    'user_1', CASE WHEN user_id_1 = _user_id THEN _user_id END,
                                    'user_2', CASE WHEN user_id_2 = _user_id THEN _user_id END,
                                    'seen', seen::TEXT
                                )
                        ) AS voice
             FROM repbot_schema.voice_activity
             WHERE user_id_1 = _user_id
                OR user_id_2 = _user_id
         ),
         cleanup_tasks AS (
             SELECT jsonb_agg(
                            jsonb_build_object(
                                    'guild', guild_id,
                                    'user', user_id,
                                    'delete_after', delete_after::text
                                )
                        ) AS cleanup
             FROM repbot_schema.cleanup_schedule c
             WHERE c.user_id = _user_id
         ),
         gdpr_log AS (
             SELECT jsonb_build_object(
                            'user', user_id,
                            'received', received,
                            'attempts', attempts,
                            'requested', requested
                        ) AS gdpr
             FROM gdpr_log l
             WHERE l.user_id = _user_id
         )
    SELECT jsonb_pretty(
                   jsonb_build_object(
                           'reputation', coalesce(rep, '[]'::JSONB),
                           'voice_activity', coalesce(voice, '[]'::JSONB),
                           'cleanup_tasks', coalesce(cleanup, '[]'::jsonb),
                           'gdpr_log', coalesce(gdpr_log, '{}'::jsonb)
                       )
               )
    FROM reputation,
         voice_activity,
         cleanup_tasks,
         gdpr_log
    INTO _result;
    RETURN _result;
END;
$BODY$;
