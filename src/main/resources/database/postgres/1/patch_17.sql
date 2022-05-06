alter table repbot_schema.guild_settings
    drop column IF EXISTS manager_role;

alter table repbot_schema.guild_settings
    drop column IF EXISTS prefix;
