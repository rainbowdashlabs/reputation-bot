---
title: Managing the bot
sidebar_position: 8
---

You manage your running bot with the `/bot` command. It is in English only.

## Where and who

- `/bot` exists only on your **bot guild**, the server in `baseSettings.botGuild`.
- By default, only members with the **Administrator** permission on that server see and use it. You
  can change that under **Server Settings > Integrations**.
- `restart`, `upgrade` and `shutdown` also check that you are a **bot owner** (`baseSettings.botOwner`).
  Everyone else gets "No.".

:::warning
All other subcommands don't check for bot owners. Anyone who can use `/bot` on your bot guild can
look into other servers, leave them or send messages there. Give the Administrator permission on your
bot guild only to people you trust.
:::

## System

| Command | What it does |
|---|---|
| `/bot system status` | Shows system stats of the bot. |
| `/bot system metrics` | Pages through charts of reputation, commands, messages and users. The charts load from `api.url`. |
| `/bot system reload` | Reads `config.json` again. See [configuration](./configuration.md#how-it-works). |
| `/bot system restart` | Ends the bot with exit code `10`. |
| `/bot system upgrade` | Ends the bot with exit code `20`. |
| `/bot system shutdown` | Ends the bot with exit code `0`. |

### Restart, upgrade and shutdown

These three only end the process. The exit code tells a start script what to do next: `10` means
start again, `20` means update and start again, `0` means stay off. The bot does not restart or
update itself.

When the bot runs in Docker, these three are hidden. The image sets `DOCKER=true`, and the bot
leaves them out. Use Docker instead:

```bash
docker compose restart app
docker compose stop app
```

With `restart: unless-stopped`, Docker also starts the bot again after a crash. To update, see
[upgrading](./upgrading.md).

When you run the bot [from source](./from-source.md), wrap it in a script that reads the exit code,
for example:

```bash
while true; do
  java -Dbot.config=config/config.json -Dlog4j2.configurationFile=config/log4j2.xml -Dcjda.localisation.error.name=false -jar bot.jar
  code=$?
  if [ "$code" -eq 10 ]; then continue; fi
  if [ "$code" -eq 20 ]; then ./update.sh; continue; fi
  break
done
```

`update.sh` stands for whatever you use to build or download a new `bot.jar`.

## Servers

| Command | What it does |
|---|---|
| `/bot search <term>` | Lists the servers whose name contains the term. |
| `/bot debug <guild_id> [channel_id]` | Shows information, permissions and settings of a server, and the permissions in one channel. |
| `/bot shared_guilds [user_id] [user] [deep]` | Lists the servers the bot shares with a user, and their role there. `deep` also asks Discord for every server one by one. |
| `/bot session <guild_id>` | Gives you a dashboard link for that server. Bot owners can open the dashboard of every server the bot is in. |
| `/bot invalidate_cache <guild>` | Drops the cached data of a server, so it is loaded fresh from the database. |
| `/bot leave <guild_id>` | Makes the bot leave a server. |
| `/bot remove_nickname <guild_id>` | Removes the bot's nickname on a server. |
| `/bot remove_profile_picture <guild_id>` | Removes the bot's profile picture on a server. |
| `/bot system_message guild <guild_id>` | Opens a form and posts your text in the system channel of that server. |
| `/bot system_message all` | Not implemented yet. |

## Commands

| Command | What it does |
|---|---|
| `/bot interactions get <guild_id>` | Lists the global commands and the commands registered on that server. |
| `/bot interactions redeploy <guild_id>` | Registers the commands of that server again. |

## Logs

| Command | What it does |
|---|---|
| `/bot log analyzer <guildid> <messageid>` | Meant to show how the bot analyzed a message. |

:::warning
`/bot log analyzer` fails with an error right now.
:::

## Entitlements

These manage Discord test entitlements for the SKUs in `skus.subscriptions`. You only need them if
your application sells something in the Discord shop.

| Command | What it does |
|---|---|
| `/bot entitlement create <sku> [ownerid]` | Creates a test entitlement for a server. Without `ownerid` it uses the current server. |
| `/bot entitlement list [guild_id]` | Lists the active entitlements of a server. |
| `/bot entitlement delete <entitlementid>` | Deletes a test entitlement. |

## Development

| Command | What it does |
|---|---|
| `/bot sample` | Fills the current server with random reputation of up to 100 members, for testing. |

:::danger
`/bot sample` writes a lot of fake reputation into the server you run it on. There is no command to undo it.
Use it only on a test server.
:::
