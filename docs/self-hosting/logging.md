---
title: Logging
sidebar_position: 7
---

The bot logs with Log4j 2. It reads its setup from the file given with
`-Dlog4j2.configurationFile`. The Docker image uses `config/log4j2.xml`.

If you mount your own config folder, copy the file out of the image first. See
[Docker Compose](./docker-compose.md#4-fill-the-config-folder). When you run
[from source](./from-source.md), start with `docker/config/log4j2.xml` from the repository.

## What the file sets up

| Appender | Where it writes |
|---|---|
| `ConsoleDebug` | The console, in color. This is what `docker compose logs` shows. |
| `RollingFile` | `logs/app.log`. A new file starts every day and on every start. The last 10 are kept. |
| `DiscordVerbose` | A Discord webhook. Gets every message from level `INFO` up. |
| `DiscordStatus` | A Discord webhook. Gets status messages: the bot joining and leaving servers, shard connects, restarts, shutdowns, upgrades and the self cleanup. |
| `DiscordError` | A Discord webhook. Gets errors meant for the admin, like failed commands and database errors. |
| `socket` | JSON over UDP to `localhost:8888`, for a log collector. |

## Logs in a Discord channel

1. Create a private channel on your server.
2. In the channel settings, under **Integrations > Webhooks**, create a webhook and copy its URL.
3. Paste the URL into the `url` of one of the `DiscordWebhook` appenders:

   ```xml
   <DiscordWebhook name="DiscordError"
                   url="https://discord.com/api/webhooks/123456789012345678/abcdef">
       <MarkerFilter marker="NOTIFY_ADMIN" onMatch="ACCEPT" onMismatch="DENY"/>
   </DiscordWebhook>
   ```

4. Restart the bot.

Use one channel per webhook, or point several appenders to the same webhook. `DiscordError` is the
most useful one: it tells you when something breaks.

The bot collects messages for a few seconds and sends them together, because Discord limits how
often a webhook may be used. `DiscordVerbose` can still be a lot.

:::note
An appender with an empty `url` doesn't work, and Log4j says so on start. The bot runs fine anyway.
To get rid of the message, remove the appender and its `AppenderRef` at the bottom of the file.
:::

## The socket appender

`socket` sends every log line as JSON to `localhost`, UDP port 8888. That is meant for a log
collector on the same machine. Without one, the messages go nowhere. Remove the appender and its
`AppenderRef` if you don't use it.

## Log levels

The levels are set at the bottom of the file:

```xml
<Loggers>
    <Logger name="net.dv8tion.jda" level="INFO"/>
    <Logger name="org.eclipse.jetty" level="INFO"/>
    <Logger name="com.zaxxer.hikari" level="INFO"/>
    <Logger name="de.chojo.jdautil" level="INFO"/>

    <Root level="TRACE">
        ...
    </Root>
</Loggers>
```

The root level is `TRACE`, so the bot's own messages are logged in full detail. The libraries for
Discord, the web server, the database pool and the command framework are kept at `INFO`.

To log less, raise the root level to `DEBUG` or `INFO`. To look closer at one part, add a `Logger`
for its package, for example:

```xml
<Logger name="de.chojo.repbot.analyzer" level="TRACE"/>
```

The levels from most to least detail are `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`.
