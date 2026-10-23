---
title: Docker Compose
sidebar_position: 3
---

The quickest way to run your own bot: the published image, a PostgreSQL container, one Compose file
and a config folder.

## The image

`ghcr.io/rainbowdashlabs/reputation-bot` holds the bot, its API and the dashboard. `latest` is built
from the `main` branch. Every build is also tagged with its full commit SHA, if you want to pin one.

Inside the container, the bot runs in `/app`:

| Path | What it is |
|---|---|
| `/app/config/config.json` | The [configuration](./configuration.md). |
| `/app/config/log4j2.xml` | The [logging setup](./logging.md). |
| `/app/logs/` | Log files. |
| `/app/public/` | The dashboard. |
| `/app/assets/` | The Markdown pages behind `/tos` and `/privacy`. |

The bot serves the dashboard and its API on port 8888.

## 1. Make a folder

```text
repbot/
  compose.yaml
  .env
  config/
    config.json
    log4j2.xml
```

`logs/` is created on the first start.

## 2. Write `compose.yaml`

```yaml
services:
  app:
    image: ghcr.io/rainbowdashlabs/reputation-bot:latest
    restart: unless-stopped
    depends_on:
      - database
    env_file: .env
    environment:
      BOT_DB_HOST: database
      BOT_DB_PORT: "5432"
      BOT_DB_DATABASE: repbot
      BOT_DB_SCHEMA: repbot_schema
      BOT_DB_USER: repbot
      BOT_API_URL: https://repbot.example.com
    ports:
      - 127.0.0.1:8888:8888
    volumes:
      - ./config:/app/config
      - ./logs:/app/logs

  database:
    image: postgres:18
    restart: unless-stopped
    environment:
      POSTGRES_USER: repbot
      POSTGRES_PASSWORD: ${BOT_DB_PASSWORD}
      POSTGRES_DB: repbot
    volumes:
      - db_data_18:/var/lib/postgresql

volumes:
  db_data_18:
```

What matters in it:

- **`ports`** publishes the bot on port 8888 of the host, only on `127.0.0.1`. Your reverse proxy
  reaches it there. Write `8888:8888` instead if you want to reach it from other machines directly.
- **`./config`** replaces the config folder of the image. The image's own `config.json` and
  `log4j2.xml` are hidden by it, so you put both files there yourself.
- **`./logs`** keeps the log files when the container is replaced.
- **`BOT_DB_*`** tell the bot where the database is. They win over the `database` section of
  `config.json`.
- **`BOT_API_URL`** is the public address of your dashboard. See [reverse proxy](#reverse-proxy).
- The database keeps its data in the volume `db_data_18`. PostgreSQL 18 expects the volume at
  `/var/lib/postgresql`, not at `/var/lib/postgresql/data` like older versions.
- The bot does not wait for the database to be ready. If it can't connect, it tries again every 10
  seconds.

## 3. Write `.env`

The secrets, kept out of `compose.yaml` and `config.json`. Compose passes all of them to the bot,
and uses `BOT_DB_PASSWORD` for the database too.

```properties
BOT_TOKEN=your-bot-token
BOT_DB_PASSWORD=a-long-random-password
BOT_DISCORD_CLIENT_ID=your-client-id
BOT_DISCORD_CLIENT_SECRET=your-client-secret
BOT_DISCORD_REDIRECT_URI=https://repbot.example.com/v1/auth/discord/callback
BOT_GRANT_ALL_SKU=true
```

Where these come from is on the [Discord application](./discord-application.md) page.
`BOT_GRANT_ALL_SKU=true` unlocks every [supporter feature](../supporter.md). Leave it out if you
don't want that.

## 4. Fill the config folder

Copy the logging setup out of the image:

```bash
mkdir -p config
docker run --rm --entrypoint cat ghcr.io/rainbowdashlabs/reputation-bot:latest config/log4j2.xml > config/log4j2.xml
```

Then write `config/config.json` with at least your own ids and invite link:

```json
{
  "baseSettings": {
    "botOwner": [123456789012345678],
    "botGuild": 234567890123456789
  },
  "links": {
    "invite": "https://discord.com/oauth2/authorize?client_id=YOUR_CLIENT_ID&scope=bot+applications.commands&permissions=4503875914918976"
  }
}
```

You don't need to list every setting. On every start the bot reads the file, fills in every missing
setting with its default, and writes the whole file back. The [configuration](./configuration.md)
page explains all of them.

:::warning
Create `config.json` before the first start. If the file is missing, the bot writes one with
default values and stops with "Please configure the config.". With `restart: unless-stopped`
Docker then starts it again right away, and it runs with those defaults: no bot owner and no bot
guild.
:::

## 5. Start it

```bash
docker compose up -d
docker compose logs -f app
```

On the first start the bot creates its tables in the database, connects to Discord and opens port
8888. Then set up your reverse proxy and open your address.

## Reverse proxy

Put a reverse proxy with HTTPS in front of port 8888 and pass every path to it. The dashboard and
the API (`/v1/...`) live on the same address.

For example with Caddy:

```text
repbot.example.com {
    reverse_proxy 127.0.0.1:8888
}
```

Then set `BOT_API_URL` (or `api.url` in `config.json`) to exactly this public address, without a
trailing slash. The bot uses it for:

- the links to the dashboard it posts in Discord,
- sending people back to the dashboard after they log in with Discord,
- the chart links of `/bot system metrics`.

:::warning
The default `api.url` is not your address. If you don't set it, logging in to your dashboard sends
you to a different site.
:::

## The Compose file in the repository

The repository has the same file as `docker/docker-compose.yml`. You still need the `.env` file and the
config folder from this page next to it.

## Next

- [Configuration](./configuration.md) for every setting.
- [Managing](./managing.md) the running bot.
- [Upgrading](./upgrading.md) to a new image.
