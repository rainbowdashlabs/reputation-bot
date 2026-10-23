---
title: Announcements
sidebar_position: 4
---

The bot can post three kinds of messages on its own: a message when a user reaches a new
reputation role, a message when someone gives reputation, and a leaderboard that it keeps up to
date.

## Level-up announcements

When a user receives reputation and reaches a new [reputation role](./roles.md), the bot can post
"@user has reached rank @role." Level-up announcements are off by default.

The message goes either to the channel where the reputation was given, or to one fixed text
channel.

| What | Dashboard (**Announcements** page) | Command |
|---|---|---|
| Turn on or off | **Enable Announcements** | `/channel announcement state <active>` |
| Same channel or a fixed one | **Send in Same Channel** | `/channel announcement where <where>` with `same channel` or `custom channel` |
| Pick the fixed channel | **Announcement Channel** | `/channel announcement channel <channel>` |
| Show the current settings | | `/channel announcement info` |

To use a fixed channel, set both: switch the location to `custom channel` and pick the channel.
Picking a channel alone doesn't change the location. The fixed channel must be a text channel.

## Reputation messages

The bot can post a message whenever someone gives reputation. It says who gave reputation to whom
and how much reputation the receiver has now:

```text
@Donor gave Reputation to @Receiver.
@Receiver now has 12 Reputation.
```

The count follows your [reputation mode](../reputation/reputation-modes.md), so it is the same
number that rankings and roles use. The message is posted in the channel where the reputation was
given.

You turn the message on or off for each way of giving reputation. Do this in the
**Reputation Messages** part of the **Reputation** page of the [dashboard](../getting-started/dashboard.md)
settings, or with `/messages states`, which opens a menu with one entry per setting.

| Way of giving reputation | Default for a new server |
|---|---|
| Reaction | on |
| Answer | off |
| Mention | off |
| Fuzzy | off |
| Embed (called "Message" in Discord) | off |
| Direct | off |
| Command | off |

### Deleting reputation messages

By default, reputation messages are deleted again 30 seconds after they were sent. This applies to
the messages of every way of giving reputation. Turn off **Delete after 30 seconds** on the
Reputation page, or the **Delete announcements** entry of `/messages states`, to keep them in the
channel.

### Reputation given by command

Reputation given with `/reputation` works a little differently, because the command already
answers with a message. Whether everyone sees that answer is set with **Ephemeral Response** on the
Reputation page, or the **Hide reputation command confirmation** entry of `/messages states`.

- **Hidden answer**: only the user who ran the command sees it. If command messages are turned on,
  the bot posts a reputation message in the channel as well.
- **Visible answer**: the answer itself becomes the reputation message. If command messages are
  turned on, the answer carries the reputation count, and the bot sends no second message.

## Autopost

Autopost keeps a leaderboard in a text channel. It posts the top 20 users by received reputation,
using your reputation mode, and refreshes the post on a schedule. Autopost is a
[supporter feature](../supporter.md).

Set it up on the **Autopost** page of the dashboard settings, or with `/channel autopost`.

| Setting | Values | Default |
|---|---|---|
| Refresh Interval | `HOURLY` every full hour, `DAILY` at midnight UTC, `WEEKLY` at midnight UTC on Mondays, `MONTHLY` at midnight UTC on the first day of the month | `DAILY` |
| Refresh Type | `DELETE_AND_REPOST` deletes the old post and sends a new one, `REPOST` sends a new post and keeps the old one, `UPDATE` edits the existing post | `DELETE_AND_REPOST` |

| Command | What it does |
|---|---|
| `/channel autopost enable <channel> [refreshtype] [refreshinterval]` | Turns autopost on in a text channel and posts the leaderboard right away. |
| `/channel autopost disable` | Turns autopost off. |
| `/channel autopost info` | Shows the current settings. |
| `/channel autopost refresh` | Refreshes the post now. |

In the dashboard, **Send Autopost Now** does the same as `/channel autopost refresh`.

:::warning
The command options `refreshtype` and `refreshinterval` only accept the values exactly as the
autocomplete offers them, in upper case. Pick them from the list.
:::

All commands on this page need the Administrator permission by default.
