---
title: Self-hosting
sidebar_position: 1
---

Reputation Bot is open source, so you can run your own copy of it. Your copy is a separate bot with
its own name, its own database and its own dashboard. It does not share anything with the public
instance.

:::tip
You don't need to host anything to use the bot. The public instance is free and maintained for you.
See [inviting the bot](../getting-started/invite.md).
:::

## What you need

- **A Discord application.** It gives you the bot account and the login for the dashboard.
  See [Discord application](./discord-application.md).
- **A way to run it.** Either:
  - **Docker with Docker Compose.** This is the easiest way. The image
    `ghcr.io/rainbowdashlabs/reputation-bot` contains the bot, the API and the dashboard.
  - **Java 25 and Node.js 24** to build and run it yourself.
- **PostgreSQL.** The bot keeps everything in PostgreSQL 18. The Compose setup brings its own
  database.
- **An address for the dashboard**, if you want to use it from other machines. The bot serves the
  dashboard and its API on port 8888. Put a reverse proxy with HTTPS in front of it.

## In what order

1. [Create the Discord application](./discord-application.md) and copy its token, client id and
   client secret.
2. Run the bot, either with [Docker Compose](./docker-compose.md) or
   [from source](./from-source.md).
3. Adjust the [configuration](./configuration.md). That page lists every setting.
4. Look at the [database](./database.md) page for permissions and backups, and at
   [logging](./logging.md) if you want logs in a Discord channel.
5. [Manage the bot](./managing.md) with the `/bot` command on your bot guild.
6. [Upgrade](./upgrading.md) when a new version is out.

Once your bot is online, everything else works like on the public instance. Start with the
[first setup](../getting-started/first-setup.md).

## Supporter features

Your own bot has no Discord shop. Set `BOT_GRANT_ALL_SKU=true` to unlock every
[supporter feature](../supporter.md) on every server your bot is in. See
[configuration](./configuration.md#settings-outside-the-file).
