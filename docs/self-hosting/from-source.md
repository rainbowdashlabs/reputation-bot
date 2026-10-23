---
title: From source
sidebar_position: 4
---

Build the bot and the dashboard yourself and run them without Docker. You need:

- a JDK 25,
- Node.js 24 with npm,
- a PostgreSQL 18 database. See [database](./database.md).

## 1. Get the source

```bash
git clone https://github.com/rainbowdashlabs/reputation-bot.git
cd reputation-bot
```

## 2. Build the bot

```bash
./gradlew shadowJar
```

This builds one jar with everything in it: `build/libs/rep-bot-<version>-all.jar`.

## 3. Build the dashboard

```bash
cd frontend
npm ci
npm run build
cd ..
```

The dashboard ends up in `frontend/dist`.

## 4. Put it together

The bot looks for its files relative to the folder you start it in. Make a folder like this:

```text
repbot/
  bot.jar
  public/
  assets/
  config/
    config.json
    log4j2.xml
```

```bash
mkdir -p ../repbot/config
cp build/libs/rep-bot-*-all.jar ../repbot/bot.jar
cp -r frontend/dist ../repbot/public
cp -r assets ../repbot/assets
cp docker/config/log4j2.xml ../repbot/config/
```

| Path | What it is |
|---|---|
| `public/` | The dashboard. The bot serves it when this folder exists. |
| `assets/` | The Markdown pages behind `/tos` and `/privacy`. Replace them with your own. |
| `config/config.json` | The [configuration](./configuration.md). |
| `config/log4j2.xml` | The [logging setup](./logging.md). |

Write `config/config.json` as shown on the [configuration](./configuration.md#a-starting-file) page.
The bot creates `logs/` on start.

:::note
Without a `public/` folder, the dashboard address shows a short page saying that something is
wrong. The API still works.
:::

## 5. Run it

Start the bot from the `repbot` folder, with the same flags the Docker image uses:

```bash
cd ../repbot
java -Dbot.config=config/config.json -Dlog4j2.configurationFile=config/log4j2.xml -Dcjda.localisation.error.name=false -jar bot.jar
```

Give the secrets as environment variables, for example `BOT_TOKEN`, `BOT_DB_PASSWORD` and
`BOT_DISCORD_CLIENT_SECRET`. See [configuration](./configuration.md).

The bot listens on port 8888. Put a reverse proxy in front of it and set `api.url`, as described on
the [Docker Compose](./docker-compose.md#reverse-proxy) page.

## Restarts

Outside Docker, `/bot system` has `restart`, `upgrade` and `shutdown`. They only end the process,
each with its own exit code. Something around the bot has to start it again. See
[managing the bot](./managing.md#restart-upgrade-and-shutdown).
