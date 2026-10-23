---
title: Configuration
sidebar_position: 5
---

Every setting of your bot instance. The settings of a single server are not here: server admins
change those in the [dashboard](../getting-started/dashboard.md).

## How it works

The bot reads one JSON file. Its path is given with the system property `bot.config`, either absolute
or relative to the working directory. The Docker image uses `config/config.json` in `/app`.

On every start, and on `/bot system reload`:

1. If the file is missing, the bot writes it with all defaults and stops with
   "Please configure the config.".
2. Otherwise it reads the file. Missing settings get their default, unknown keys are ignored.
3. It writes the complete file back, with every setting.

So you only write the settings you want to change, and the bot adds the rest. A new version adds
its new settings the same way.

Many settings can also come from an environment variable or a system property. The order is:

1. environment variable,
2. system property (`-D...`),
3. `config.json`,
4. the default.

Values from the environment are never written into the file. That makes the environment the right
place for the token and passwords.

:::tip
`/bot system reload` reads the file again while the bot runs. Settings the bot only uses at start,
like the token, the database and the API host and port, still need a restart.
:::

## A starting file

```json
{
  "baseSettings": {
    "botOwner": [123456789012345678],
    "botGuild": 234567890123456789
  },
  "api": {
    "url": "https://repbot.example.com"
  },
  "discordOAuth": {
    "clientId": "YOUR_CLIENT_ID",
    "redirectUri": "https://repbot.example.com/v1/auth/discord/callback"
  },
  "links": {
    "invite": "https://discord.com/oauth2/authorize?client_id=YOUR_CLIENT_ID&scope=bot+applications.commands&permissions=4503875914918976"
  }
}
```

Give the token, the client secret and the database password as `BOT_TOKEN`,
`BOT_DISCORD_CLIENT_SECRET` and `BOT_DB_PASSWORD`.

## baseSettings

| Key | Default | Environment | What it does |
|---|---|---|---|
| `token` | `""` | `BOT_TOKEN` | The bot token. **Required.** |
| `botOwner` | `[]` | | Discord user ids of the bot owners. Only they can use `/bot`, and they can open the dashboard of every server the bot is in. |
| `botGuild` | `0` | | The id of your own server. `/bot` is registered only there. **Required.** |
| `privateSupportChannel` | `0` | | A text channel on the bot guild. When someone sends the bot a direct message, it offers to open a support thread there, and forwards messages both ways. Each user has one open thread at a time; locking or archiving it closes the request. With `0`, or without a `botGuild`, the bot ignores direct messages. |
| `reviewChannel` | `0` | | A text channel on the bot guild. The bot posts there when a server changes the bot's nickname. `0` turns it off. |

## database

PostgreSQL. See [database](./database.md).

| Key | Default | Environment | What it does |
|---|---|---|---|
| `host` | `"localhost"` | `BOT_DB_HOST` | Database host. |
| `port` | `"5432"` | `BOT_DB_PORT` | Database port. A string in the file. |
| `database` | `"postgres"` | `BOT_DB_DATABASE` | Database name. |
| `schema` | `"repbot_schema"` | `BOT_DB_SCHEMA` | Schema for all tables. Created if missing. |
| `user` | `"postgres"` | `BOT_DB_USER` | Database user. |
| `password` | `"postgres"` | `BOT_DB_PASSWORD` | Database password. |
| `poolSize` | `5` | `BOT_DB_POOLSIZE` | Maximum number of database connections. |

## api

The web server for the dashboard and its API.

| Key | Default | Environment | What it does |
|---|---|---|---|
| `host` | `"0.0.0.0"` | `BOT_API_HOST` | Address to listen on. |
| `port` | `8888` | `BOT_API_PORT` | Port to listen on. |
| `url` | `"https://repbot.chojo.dev"` | `BOT_API_URL` | The public address of your dashboard, without a trailing slash. Used for dashboard links in Discord, the return after Discord login, and the `/bot system metrics` charts. **Set it.** |
| `tokenValidHours` | `720` | | How long a dashboard login stays valid without being used, in hours. Expired logins are deleted every hour. |

## discordOAuth

Login with Discord for the dashboard. See [Discord application](./discord-application.md#3-login-for-the-dashboard).

| Key | Default | Environment | What it does |
|---|---|---|---|
| `clientId` | `""` | `BOT_DISCORD_CLIENT_ID` | The client id of your application. |
| `clientSecret` | `""` | `BOT_DISCORD_CLIENT_SECRET` | The client secret. |
| `redirectUri` | `""` | `BOT_DISCORD_REDIRECT_URI` | Your address with `/v1/auth/discord/callback`. Must match the redirect in the portal. |

## links

Links the bot shows in `/info`, `/invite`, its leave message and the dashboard.

| Key | Default | What it does |
|---|---|---|
| `tos` | `""` | Terms of service. |
| `invite` | the public instance's invite | Invite link of the bot. **Set it** to your own. |
| `support` | `""` | Support server. |
| `website` | `"https://docs.chojo.dev/reputation-bot"` | Website. |
| `faq` | `"https://docs.chojo.dev/reputation-bot/faq"` | FAQ. |
| `kofi` | `"https://ko-fi.com/reputationbot"` | Ko-fi page. |

The dashboard pages `/tos` and `/privacy` show the Markdown files in the `assets` folder next to the
bot (`/app/assets` in the image). They contain the texts of the public instance. Replace them with
your own if your bot is used by others.

## selfcleanup

The bot asks unused servers whether it should stay, and leaves if nobody answers. Note the lower case
key.

| Key | Default | Environment | What it does |
|---|---|---|---|
| `active` | `true` | `BOT_SELF_CLEANUP`, `bot.cleanup` | Turns the self cleanup on or off. |
| `promptDays` | `3` | | Only shown in the prompt message. |
| `leaveDays` | `3` | | Days after the prompt until the bot leaves. |
| `inactiveDays` | `90` | | A server is unused when the bot joined more than this many days ago, and either the last reputation is older than this, or the server limits thanking to listed channels and lists none. Servers that never had any reputation are never prompted. |

:::tip
For a bot that only serves your own servers, set `BOT_SELF_CLEANUP=false`.
:::

## cleanup

How long the bot keeps temporary data.

| Key | Default | What it does |
|---|---|---|
| `analyzerLogHours` | `24` | Hours the message analyzer results are kept. |
| `gdprDays` | `90` | Days a finished data request is kept in the log. |
| `voiceActivityHours` | `24` | Hours voice activity is kept for abuse protection. |
| `cleanupScheduleDays` | `14` | Days after which the data of a server the bot left is deleted. |

## analyzerSettings

How the bot looks at a message to find out who is thanked.

| Key | Default | What it does |
|---|---|---|
| `historySize` | `100` | How many earlier messages the bot reads. At most 100. |
| `voiceMembers` | `10` | How many recent voice channel members count as context. |
| `latestMaxHours` | `12` | How old a message may be to count as recent. |
| `minFuzzyScore` | `0.9` | How close a fuzzy match must be, from 0 to 1. |

## magicImage

When someone thanks themselves in a message, the bot sometimes replies with an image.

| Key | Default | What it does |
|---|---|---|
| `magicImageLink` | `""` | Link to the image. Empty turns the image off. |
| `magicImagineChance` | `10` | The chance is one in this number. `0` turns the image off. |
| `magicImageCooldown` | `30` | Minutes between two images. |
| `magicImageDeleteSchedule` | `60` | Seconds until the image is deleted again. |

## badges

| Key | Default | What it does |
|---|---|---|
| `enables` | `true` | Shows badges on profiles. Note the spelling of the key. |
| `badges` | `[]` | Image links. The first is shown on the profile of rank 1, the second on rank 2, and so on. |

## presenceSettings

The status of the bot in Discord.

| Key | Default | What it does |
|---|---|---|
| `active` | `false` | Changes the status regularly. |
| `interval` | `5` | Minutes between two changes. |
| `status` | seven custom statuses | A list of `{"type": ..., "text": ...}`. The bot picks one at random. |

`type` is a Discord activity type: `PLAYING`, `STREAMING`, `LISTENING`, `WATCHING`,
`CUSTOM_STATUS` or `COMPETING`. The text can use these placeholders: `%guild_count%`,
`%channel_count%`, `%total_rep%`, `%weekly_rep%`, `%today_rep%`, `%weekly_avg_rep%`,
`%analyzed_messages%`.

## botlist

For listing the bot on bot list sites. Your own bot can leave this section alone.

| Key | Default | What it does |
|---|---|---|
| `submit` | `false` | `true` posts the server count to the bot lists every hour. With `false` it posts only every 60 days. |
| `topGg`, `discordBotsGg`, `discordBotlistCom`, `botListMe` | | One block per site: `name`, `statsToken`, `voteToken`, `guildId`, `profileUrl`, `voteUrl`. |

## voting

Tokens for voting on bot lists.

| Key | Default | What it does |
|---|---|---|
| `minDaysStreak` | `7` | Days of voting in a row before a vote gives double tokens. |
| `hoursSteak` | `28` | Hours a vote may be apart from the last one to keep the streak. Note the spelling of the key. |

## kofi

| Key | Default | What it does |
|---|---|---|
| `token` | `null` | The verification token of your Ko-fi webhook. Ko-fi sends payments to `/v1/kofi`. Without a token, the bot rejects every payment it receives. |

## mailing

The bot sends mail to verify the mail addresses users add in the dashboard, and about Ko-fi purchases. Your own bot doesn't need it.

| Key | Default | What it does |
|---|---|---|
| `mailSalt` | random | Salt for storing mail addresses as hashes. Created on the first start. Don't change or lose it. |
| `smtp` | `{"host": "", "port": 665, "ssl": false}` | Server for sending. |
| `imap` | `{"host": "", "port": 665, "ssl": false}` | Server where sent mails are stored. |
| `user` | `""` | Mail account, also the sender address. |
| `password` | `""` | Its password. |
| `properties` | `{}` | Extra JavaMail properties. |

## skus

Discord shop items and the [supporter features](../supporter.md) they unlock. This is how the public
instance sells features. For your own bot, use `BOT_GRANT_ALL_SKU=true` instead and leave this
section alone.

| Key | Default | What it does |
|---|---|---|
| `subscriptions` | one empty entry | Each with `name`, `kofiSubscriptionId`, `kofiLifetimeId`, `subscriptionSku` and `lifetimeSku`. |
| `interactions` | `{"slash": {}, "messages": {}, "users": {}}` | Commands that need a SKU, as command name to a list of SKU ids. |
| `features` | one block per feature | Each with `id`, `tokens` (price per month in the token shop), `localeKey` and `skuEntry` (SKU ids that unlock it). |
| `subscriptionErrorMessageHours` | `36` | Hours between two messages about a missing subscription. |
| `errorThresholdBlock` | `10` | Together with the hours above, sets the time left that the missing subscription message shows. |

The feature blocks are `reputationLog`, `analyzerLog`, `reputationCategories`, `reputationChannel`,
`channelBlacklist`, `localeOverrides`, `autopost`, `advancedRankings`, `detailedProfile`,
`logChannel`, `additionalEmojis`, `profile`, `integrationBypass` and `commandAliases`. Some have one
more key: `reputationLog.defaultSize`, `analyzerLog.extendedLogHours`,
`reputationCategories.defaultCategories` and `reputationChannel.defaultChannel`.

## Settings outside the file

These exist only as environment variables or system properties.

| Environment | System property | Default | What it does |
|---|---|---|---|
| `BOT_GRANT_ALL_SKU` | `bot.grantallsku` | `false` | `true` unlocks every supporter feature on every server. |
| `BOT_GDPR_ENABLE` | `bot.gdpr.enable` | `true` | `false` stops the hourly data job: it sends requested data exports, queues the data of servers the bot left for deletion, and deletes it after `cleanup.cleanupScheduleDays`. |
| `DOCKER` | `docker` | `false` | `true` hides `/bot system restart`, `upgrade` and `shutdown`. The image sets it. Here the property wins over the variable. |
| `CJDA_INTERACTIONS_TESTMODE` | `cjda.interactions.testmode` | `false` | For development. Registers the commands per server instead of globally. |
| `CJDA_INTERACTIONS_CLEANGUILDCOMMANDS` | `cjda.interactions.cleanguildcommands` | `false` | For development. Redeploys the commands of every server on start. |
| `CJDA_PREMIUM_SKIP_ENTITLED_CHECK` | `cjda.premium.skipEntitledCheck` | `false` | For development. Skips the SKU check of commands listed in `skus.interactions`. |
| | `bot.config` | none | Path of `config.json`, absolute or relative to the working directory. **Required.** |
| | `log4j2.configurationFile` | none | Path of the [logging setup](./logging.md). |
| | `cjda.localisation.error.name` | | `false` hides warnings about untranslated command names. The image sets it. |

The image starts the bot like this:

```bash
java -Dbot.config=config/config.json -Dlog4j2.configurationFile=config/log4j2.xml -Dcjda.localisation.error.name=false -jar bot.jar
```
