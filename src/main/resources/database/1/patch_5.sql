alter table repbot_schema.cleanup_schedule
    alter column guild_id drop not null;

CREATE OR REPLACE FUNCTION repbot_schema.aggregate_user_data(user_id bigint)
    returns jsonb
    language plpgsql
    cost 100
AS
$BODY$
DECLARE
    _result jsonb;
BEGIN
    with reputation as (
        SELECT jsonb_agg(jsonb_build_object(
                'guild', guild_id,
                'channel', channel_id,
                'donor', case when donor_id = user_id then user_id end,
                'receiver', case when receiver_id = user_id then user_id end,
                'message', message_id,
                'ref_message', ref_message_id,
                'cause', cause,
                'received', received::text
            )) as rep
        from repbot_schema.reputation_log l
        where l.receiver_id = user_id
           OR l.donor_id = user_id
    ),
         voice_activity as (
             select jsonb_agg(jsonb_build_object(
                     'guild', guild_id,
                     'user_1', case when user_id_1 = user_id then user_id end,
                     'user_2', case when user_id_2 = user_id then user_id end,
                     'seen', seen::text
                 )) as voice
             from repbot_schema.voice_activity
         )
    SELECT jsonb_build_object(
                   'reputation', coalesce(rep, '[]'::jsonb),
                   'voice_activity', coalesce(voice, '[]'::jsonb)
               )
    from reputation,
         voice_activity
    into _result;
    return _result;
END;
$BODY$;

create table IF NOT EXISTS repbot_schema.gdpr_log
(
    user_id  bigint not null,
    received timestamp
);

create unique index IF NOT EXISTS gdpr_log_user_id_uindex
    on repbot_schema.gdpr_log (user_id);
