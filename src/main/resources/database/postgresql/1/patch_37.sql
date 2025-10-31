DROP VIEW repbot_schema.truncated_reputation_offset;
DROP VIEW repbot_schema.truncated_reputation_log;

ALTER TABLE repbot_schema.guild_settings
    ALTER COLUMN reset_date TYPE TIMESTAMP;

-- WITH RECURSIVE viewids AS (
--    /* all views that don't depend on other views */
--    SELECT t.oid, 1 as level
--    FROM pg_class t
--       JOIN pg_rewrite AS r ON r.ev_class = t.oid
--    WHERE r.rulename = '_RETURN'
--      AND t.relkind = 'v'
--      AND t.relnamespace NOT IN ('pg_catalog'::regnamespace,
--                                 'information_schema'::regnamespace,
--                                 'pg_toast'::regnamespace)
--      AND pg_get_viewdef(t.oid::regclass) ILIKE '%repbot_schema.guild_settings%'
--      AND pg_get_viewdef(t.oid::regclass) ILIKE '%reset_date%'
--      AND NOT EXISTS (
--             /* depends on a view */
--             SELECT 1
--             FROM pg_depend AS d
--                JOIN pg_class AS t2 ON d.refobjid = t2.oid
--             WHERE d.objid = r.oid
--               AND d.classid = 'pg_rewrite'::regclass
--               AND d.refclassid = 'pg_class'::regclass
--               AND d.deptype = 'n'
--               AND d.refobjsubid <> 0
--               AND t2.relkind = 'v'
--          )
--      AND NOT EXISTS (
--             /* depends on an extension */
--             SELECT 1
--             FROM pg_depend
--             WHERE objid = t.oid
--               AND classid = 'pg_class'::regclass
--               AND refclassid = 'pg_extension'::regclass
--               AND deptype = 'e'
--          )
-- UNION ALL
--    /* all views that depend on these views */
--    SELECT t.oid, viewids.level + 1
--    FROM pg_class AS t
--       JOIN pg_rewrite AS r ON r.ev_class = t.oid
--       JOIN pg_depend AS d ON d.objid = r.oid
--       JOIN viewids ON viewids.oid = d.refobjid
--    WHERE t.relkind = 'v'
--      AND r.rulename = '_RETURN'
--      AND d.classid = 'pg_rewrite'::regclass
--      AND d.refclassid = 'pg_class'::regclass
--      AND d.deptype = 'n'
--      AND d.refobjsubid <> 0
-- )
-- /* order the views by level, eliminating duplicates */
-- SELECT format('CREATE VIEW %s AS%s',
--               oid::regclass,
--               pg_get_viewdef(oid::regclass))
-- FROM viewids
-- GROUP BY oid
-- ORDER BY max(level);

CREATE VIEW repbot_schema.truncated_reputation_offset AS SELECT l.guild_id,
    l.user_id,
    (sum(l.amount))::bigint AS amount
   FROM (repbot_schema.reputation_offset l
     LEFT JOIN repbot_schema.guild_settings s ON ((l.guild_id = s.guild_id)))
  WHERE ((s.reset_date IS NULL) OR (l.added > s.reset_date) OR (s.reset_date > (now())::date))
  GROUP BY l.guild_id, l.user_id;

CREATE VIEW repbot_schema.truncated_reputation_log AS SELECT l.guild_id,
    l.receiver_id,
    l.donor_id
   FROM (repbot_schema.reputation_log l
     LEFT JOIN repbot_schema.guild_settings s ON ((l.guild_id = s.guild_id)))
  WHERE ((s.reset_date IS NULL) OR (l.received > s.reset_date) OR (s.reset_date > (now())::date));
