---
title: Upgrading
sidebar_position: 9
---

## Update the image

With the [Compose setup](./docker-compose.md), pull the new image and recreate the container:

```bash
docker compose pull
docker compose up -d
```

`latest` follows the `main` branch. If you pinned a commit tag, change the tag in `compose.yaml`
first.

[Back up the database](./database.md#backups) before you update. You can't go back to an older
version without a backup.

## What happens on start

You don't need to do anything else:

- The bot updates the database itself. It applies every missing update before it connects to
  Discord. See [migrations](./database.md#migrations).
- The bot adds new settings to `config.json` with their defaults. Check the file after an update to
  see what is new. See [configuration](./configuration.md#how-it-works).

Watch the first start with `docker compose logs -f app`.

When you run [from source](./from-source.md), pull the new code, build the jar and the dashboard
again, replace `bot.jar` and `public/`, and restart.

## From PostgreSQL 17 to 18

The Compose file of the repository used to run PostgreSQL 17 with the volume `db_data`. It now runs
PostgreSQL 18 with the volume `db_data_18`, like the file on the [Docker Compose](./docker-compose.md)
page. PostgreSQL 18 can't read the data of 17, so you move it over once with a dump.

The volume names start with the Compose project name, which is the folder name by default. Check
yours with `docker volume ls`. The commands below assume `docker_db_data`, and the user `root` and
the database `repbot` of the old file. Use your own names where they differ.

1. Stop the bot and the database.

   ```bash
   docker compose stop app database
   ```

2. Dump the old database with a PostgreSQL 17 container.

   ```bash
   docker run -d --name repbot-pg17 -v docker_db_data:/var/lib/postgresql/data postgres:17
   docker exec repbot-pg17 sh -c 'until pg_isready -U root; do sleep 1; done'
   docker exec repbot-pg17 pg_dump -U root -d repbot -Fc > repbot.dump
   docker rm -f repbot-pg17
   ```

3. Switch your Compose file to `postgres:18` with the new volume, then start only the database. It
   creates the new volume and an empty database.

   ```bash
   docker compose up -d database
   ```

4. Restore the dump.

   ```bash
   docker compose exec -T database sh -c 'until pg_isready -U root; do sleep 1; done'
   docker compose exec -T database pg_restore -U root -d repbot --no-owner < repbot.dump
   ```

5. Start the bot.

   ```bash
   docker compose up -d
   ```

Once the bot runs fine, remove the old volume with `docker volume rm docker_db_data` and delete
`repbot.dump`.

:::tip
The current Compose file, in the repository and on the [Docker Compose](./docker-compose.md) page, uses
the user `repbot` instead of `root`. If you switch to it, use `repbot` in step 4.
:::
