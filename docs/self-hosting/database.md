---
title: Database
sidebar_position: 6
---

The bot keeps everything in PostgreSQL: reputation, server settings, logs and dashboard logins. Use
PostgreSQL 18. That is the version of the Compose setup and the one the tests run against.

## Settings

The bot connects with the `database` section of `config.json`, or with the `BOT_DB_*` environment
variables. The table is on the [configuration](./configuration.md#database) page.

All tables live in one schema, `repbot_schema` by default. Choose the schema name before the first
start. The bot uses it in every table it creates, so you can't just rename it later.

If the bot can't reach the database on start, it logs "Could not connect to database. Retrying in
10." and tries again until it works.

## Permissions

The user needs to connect to the database and create a schema in it. The simplest way is to make the
user the owner of the database:

```bash
psql -U postgres -c "CREATE USER repbot WITH PASSWORD 'a-long-random-password';"
psql -U postgres -c "CREATE DATABASE repbot OWNER repbot;"
```

The bot needs no extensions and no superuser rights.

## Migrations

You never run SQL scripts by hand. On every start the bot:

1. creates the schema if it does not exist yet,
2. creates the table `repbot_version` in it, which stores the version of the database layout,
3. on an empty database, creates all tables,
4. applies every update the database is still missing, one after the other.

Each update runs in its own transaction. If one fails, the bot logs the failing statement and stops,
and the database stays at the last update that worked.

:::warning
You can't go back to an older version of the bot. If the database is newer than the bot, the bot
stops with "Database version is ahead". To go back, restore a backup made with the older version.
:::

## Backups

Back up the database with `pg_dump`. With the [Compose setup](./docker-compose.md):

```bash
docker compose exec -T database pg_dump -U repbot -d repbot -Fc > repbot-$(date +%F).dump
```

To restore it into an empty database:

```bash
docker compose stop app
docker compose exec -T database pg_restore -U repbot -d repbot --no-owner < repbot-2026-01-01.dump
docker compose start app
```

Keep a copy of `config/config.json` with your backups. It holds the `mailing.mailSalt`, which the
bot needs to find stored mail addresses again.
