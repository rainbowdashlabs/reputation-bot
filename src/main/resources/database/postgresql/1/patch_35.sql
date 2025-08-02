DROP VIEW IF EXISTS repbot_schema.guilds;
CREATE OR REPLACE VIEW repbot_schema.v_guilds AS
    SELECT
        guild_id
    FROM
        repbot_schema.reputation_log
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.abuse_protection
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.active_categories
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.active_channel
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.analyzer_results
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.announcements
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.autopost
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.cleanup_schedule
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.donor_roles
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.guild_locale_overrides
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.guild_ranks
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.guild_reactions
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.guild_settings
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.log_channel
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.message_states
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.receiver_roles
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.donor_roles
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.reputation_log
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.reputation_offset
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.reputation_results
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.reputation_settings
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.self_cleanup
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.thank_settings
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.thankwords
    UNION
    DISTINCT
    SELECT
        guild_id
    FROM
        repbot_schema.voice_activity
    ORDER BY guild_id;

CREATE TABLE repbot_schema.guilds (
    guild_id BIGINT NOT NULL
        CONSTRAINT guilds_pk
            PRIMARY KEY
);

-- Pre populate to be sure that adding the foreign keys doesnt fail.
INSERT
INTO
    repbot_schema.guilds
SELECT
    guild_id
FROM
    repbot_schema.v_guilds;

-- SELECT string_agg(format($$
-- ALTER TABLE repbot_schema.%s
--     ADD CONSTRAINT %s_guilds_guild_id_fk
--         FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;
-- $$, table_name, table_name),'')
-- FROM information_schema.columns
-- WHERE column_name = 'guild_id'
--   AND table_schema = 'repbot'
--   AND table_name IN (
--     SELECT table_name
--     FROM information_schema.tables
--     WHERE table_type = 'BASE TABLE'
--       AND table_schema = 'repbot'
--   );

ALTER TABLE repbot_schema.guild_locale_overrides
    ADD CONSTRAINT guild_locale_overrides_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.autopost
    ADD CONSTRAINT autopost_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.log_channel
    ADD CONSTRAINT log_channel_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.guild_settings
    ADD CONSTRAINT guild_settings_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.active_channel
    ADD CONSTRAINT active_channel_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.analyzer_results
    ADD CONSTRAINT analyzer_results_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.announcements
    ADD CONSTRAINT announcements_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.abuse_protection
    ADD CONSTRAINT abuse_protection_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.reputation_settings
    ADD CONSTRAINT reputation_settings_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.donor_roles
    ADD CONSTRAINT donor_roles_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.receiver_roles
    ADD CONSTRAINT receiver_roles_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.thank_settings
    ADD CONSTRAINT thank_settings_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.voice_activity
    ADD CONSTRAINT voice_activity_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.guild_reactions
    ADD CONSTRAINT guild_reactions_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.active_categories
    ADD CONSTRAINT active_categories_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.cleanup_schedule
    ADD CONSTRAINT cleanup_schedule_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.reputation_log
    ADD CONSTRAINT reputation_log_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.reputation_offset
    ADD CONSTRAINT reputation_offset_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.guild_ranks
    ADD CONSTRAINT guild_ranks_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.message_states
    ADD CONSTRAINT message_states_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.thankwords
    ADD CONSTRAINT thankwords_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.reputation_results
    ADD CONSTRAINT reputation_results_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.self_cleanup
    ADD CONSTRAINT self_cleanup_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;

ALTER TABLE repbot_schema.subscription_error
    ADD CONSTRAINT subscription_error_guilds_guild_id_fk
        FOREIGN KEY (guild_id) REFERENCES repbot_schema.guilds ON DELETE CASCADE;


CREATE OR REPLACE FUNCTION repbot_schema.register_guild_id(
)
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$BODY$
BEGIN
    INSERT
    INTO
        repbot_schema.guilds
    VALUES
        (new.guild_id)
    ON CONFLICT DO NOTHING;
    RETURN new;
END;
$BODY$;

-- SELECT string_agg(format($$
-- CREATE TRIGGER register_guild_id
--     BEFORE INSERT
--     ON repbot_schema.%s
--     FOR EACH ROW
-- EXECUTE FUNCTION repbot_schema.register_guild_id();
-- $$, table_name),'')
-- FROM information_schema.columns
-- WHERE column_name = 'guild_id'
--   AND table_schema = 'repbot'
--   AND table_name IN (
--     SELECT table_name
--     FROM information_schema.tables
--     WHERE table_type = 'BASE TABLE'
--       AND table_schema = 'repbot'
--   );

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.guild_locale_overrides
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.autopost
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.log_channel
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.guild_settings
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.active_channel
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.analyzer_results
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.announcements
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.abuse_protection
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.reputation_settings
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.donor_roles
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.receiver_roles
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.thank_settings
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.voice_activity
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.guild_reactions
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.active_categories
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.cleanup_schedule
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.reputation_log
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.reputation_offset
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.guild_ranks
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.message_states
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.thankwords
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.reputation_results
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.self_cleanup
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();

CREATE TRIGGER register_guild_id
    BEFORE INSERT
    ON repbot_schema.subscription_error
    FOR EACH ROW
EXECUTE FUNCTION repbot_schema.register_guild_id();


DROP MATERIALIZED VIEW IF EXISTS repbot_schema.data_statistics;
CREATE MATERIALIZED VIEW repbot_schema.data_statistics AS
    WITH
        latest_guild_activity AS (
            SELECT
                guild_id,
                max(received) AS latest
            FROM
                repbot_schema.reputation_log
            GROUP BY guild_id
                                 ),
        latest_channel_activity AS (
            SELECT
                channel_id,
                max(received) AS latest
            FROM
                repbot_schema.reputation_log
            GROUP BY channel_id
                                 )
    SELECT
        (
            SELECT
                count(1) AS count
            FROM
                repbot_schema.guilds
        ) AS guilds,
        (
            SELECT
                count(1)
            FROM
                latest_guild_activity
            WHERE latest > now() - '90 DAYS'::INTERVAL
        ) AS active_guilds,
        (
            SELECT
                count(1)
            FROM
                latest_channel_activity
            WHERE latest > now() - '90 DAYS'::INTERVAL
        ) AS active_channel,
        (
            SELECT
                count(1) AS count
            FROM
                repbot_schema.active_channel
        ) AS channel,
        (
            SELECT
                count(1) AS count
            FROM
                repbot_schema.reputation_log
        ) AS total_reputation,
        (
            SELECT
                count(1) AS count
            FROM
                repbot_schema.reputation_log
            WHERE received > ( now() - '1 day'::INTERVAL )
        ) AS today_reputation,
        (
            SELECT
                count(1) AS count
            FROM
                repbot_schema.reputation_log
            WHERE received > ( now() - '7 days'::INTERVAL )
        ) AS weekly_reputation,
        (
            SELECT
                count(1) / 4
            FROM
                repbot_schema.reputation_log
            WHERE received > ( now() - '28 days'::INTERVAL )
        ) AS weekly_avg_reputation;
