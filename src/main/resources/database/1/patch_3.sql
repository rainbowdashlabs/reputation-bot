-- add the missing unique constrain on table. requires deduplication.
create temp table temp_channel on commit drop as
    (select distinct guild_id, channel_id
     from repbot_schema.active_channel);

DELETE
from repbot_schema.active_channel;

create unique index if not exists active_channel_guild_id_channel_id_uindex
    on repbot_schema.active_channel (guild_id, channel_id);

insert into repbot_schema.active_channel(guild_id, channel_id)
select guild_id, channel_id
from temp_channel;
