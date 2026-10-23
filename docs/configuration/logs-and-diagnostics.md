---
title: Logs and diagnostics
sidebar_position: 6
---

When reputation doesn't show up where you expect it, these tools tell you why.

## Why did a message not give reputation?

Start with the analyzer log of that message. It shows step by step how the bot handled the message
and which check stopped it. Then check the **Problems** page of the dashboard for setup problems.

## Analyzer log

The bot analyzes every message written on your server and every reputation reaction, and keeps
the result for a while:

- `/log analyzer <messageid>` shows the result for a message id.
- Right-click a message and pick **Apps > Message Log** to see the same for that message.

The result lists the thankword found, the receivers the bot found, and the result of every check,
for example a [cooldown](./abuse-protection.md#cooldown), a user outside the
[context](./abuse-protection.md#receiver-and-donor-context) or a channel that isn't a
[reputation channel](./channels.md). Below it, you see the reputation that the message gave.

Results are kept for 24 hours. With the analyzer log [supporter feature](../supporter.md) they are
kept for 96 hours. If the bot answers "This message was not analyzed.", the result has already been deleted, or the
message was never analyzed, for example because a webhook sent it.

:::note
Older guides mention debug emojis that the bot added to messages. They no longer exist. The
analyzer log replaces them.
:::

## Reputation logs

These commands show what reputation was given. They need the Manage Messages permission by default.

| Command | What it shows |
|---|---|
| `/log received <user>` | The reputation a user received, newest first. |
| `/log donated <user>` | The reputation a user gave, newest first. |
| `/log message <messageid>` | The reputation given by one message. |
| `/log analyzer <messageid>` | The analyzer result of a message, see above. |

The user logs show 15 entries per page. Without the reputation log
[supporter feature](../supporter.md), you can browse the first 2 pages.

You can also right-click a user and pick **Apps > Received Reputation** or
**Apps > Given Reputation**.

## Log channel

The log channel gets a line for every reputation that is given, and for every reputation that is
removed because its message was deleted or its reaction removed. The log channel is a
[supporter feature](../supporter.md).

Set it up in the [dashboard](../getting-started/dashboard.md) under **Settings > Log Channel**: turn
on **Enable Log Channel** and pick a text channel under **Log Channel**.

## System channel

The system channel gets messages about the bot itself: missing permissions, changes to your
supporter status, and notes from the bot's developers. Pick an internal text channel that only your
team can read.

Set it under **Settings > General > System Channel**. The dashboard can change the channel but not
remove it. To run without a system channel, use `/channel systemchannel disable`.

Without a system channel, the bot sends missing-permission warnings to the server owner or an
admin by direct message instead. If the bot can't write to the system channel, it forgets the
channel.

## Problems page

The **Problems** tab of the dashboard settings lists everything the bot finds wrong with your setup.
A red mark next to the tab tells you that it found something. `/diagnose` gives you a link straight
to it. The command needs the Administrator permission by default.

It checks, among other things:

- permissions the bot is missing, on the whole server or in single channels and categories,
- reputation roles the bot can't assign because they sit above its own role, or that no longer
  exist,
- reputation channels and categories that no longer exist, or channels that aren't text channels,
- a missing system, announcement, autopost or log channel,
- a server without thankwords or without reputation channels,
- a very low [max message age](./abuse-protection.md#max-message-age).

Each entry has a **Go to settings** link to the section where you fix it.

## Audit log

The **Audit Log** tab of the dashboard settings lists every settings change made in the dashboard,
with who made it, when, and the old and new value.

## Scanning old messages

A scan looks through old messages for thanks and adds the reputation they would have given. Use it
after you add the bot, or after you add a channel that wasn't tracked before.

Open **Settings > Scan** and click **Start Scan**. The page shows the progress of the running scan
and the result of the last one: how many messages were scanned and how much reputation was found.

`/scan` starts a scan from Discord and gives you a link to the Scan page. It needs the
Administrator permission by default.

What a scan does:

- It reads all your [reputation channels](./channels.md) and categories, including their threads and
  archived public threads.
- It reads up to 10,000 messages per channel or thread, newest first.
- It finds reputation given by mention, answer and fuzzy matching. Reactions and embeds are not
  scanned.
- It skips messages from bots and users thanking themselves. Other abuse protection checks don't
  apply.

Only one scan per server runs at a time.
