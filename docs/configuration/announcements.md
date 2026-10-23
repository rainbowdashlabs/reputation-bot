---
title: Announcements
sidebar_position: 4
---

The bot can post three kinds of messages on its own: a message when a user reaches a new
reputation role, a message when someone gives reputation, and a leaderboard that it keeps up to
date. You set all of them up in the [dashboard](../getting-started/dashboard.md).

## Level-up announcements

When a user receives reputation and reaches a new [reputation role](./roles.md), the bot can post
"@user has reached rank @role." Level-up announcements are off by default.

Turn them on under **Settings > Announcements > Enable Announcements**. Two more settings then show
up:

| Setting | What it does |
|---|---|
| Send in Same Channel | On: the message goes to the channel where the reputation was given. Off: it goes to the announcement channel. |
| Announcement Channel | The text channel for the messages while **Send in Same Channel** is off. |

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

You turn the message on or off for each way of giving reputation under
**Settings > Reputation > Reputation Messages**:

| Toggle | Default for a new server |
|---|---|
| Reaction Reputation | on |
| Answer Reputation | off |
| Mention Reputation | off |
| Fuzzy Matching | off |
| Embed Reputation | off |
| Direct Embed Reputation | off |
| Command Reputation | off |

The **Command Reputation** toggle only shows up while
[command reputation](../reputation/giving-reputation.md#command) is turned on.

### Deleting reputation messages

By default, reputation messages are deleted again 30 seconds after they were sent. This applies to
the messages of every way of giving reputation. Turn off **Delete after 30 seconds** at the end of
the Reputation Messages list to keep them in the channel.

### Reputation given by command

Reputation given with `/reputation` works a little differently, because the command already
answers with a message. Whether everyone sees that answer is set with
**Settings > Reputation > Command Reputation > Ephemeral Response**.

- **Ephemeral Response on**: only the user who ran the command sees the answer. If the
  **Command Reputation** message is turned on, the bot posts a reputation message in the channel as
  well.
- **Ephemeral Response off**: everyone sees the answer, so the answer itself is the reputation
  message. If the **Command Reputation** message is turned on, the answer carries the reputation
  count, and the bot sends no second message.

## Autopost

Autopost keeps a leaderboard in a text channel. It posts the top 20 users by received reputation,
using your reputation mode, and refreshes the post on a schedule. Autopost is a
[supporter feature](../supporter.md).

Set it up under **Settings > Autopost**:

| Setting | What it does |
|---|---|
| Enable Autopost | Turns the leaderboard on or off. It is off by default. |
| Autopost Channel | The text channel for the leaderboard. |
| Refresh Interval | **Hourly** every full hour, **Daily** at midnight UTC, **Weekly** at midnight UTC on Mondays, **Monthly** at midnight UTC on the first day of the month. The default is Daily. |
| Refresh Type | **Delete and Repost** deletes the old post and sends a new one, **Repost** sends a new post and keeps the old one, **Update** edits the existing post. |

**Send Autopost Now** posts or updates the leaderboard right away.

:::warning
On a server that never changed the refresh type, the dashboard shows **Delete and Repost**, but the
bot stores **Update** as soon as you change any autopost setting. If you want Delete and Repost,
pick another type first and then switch back to it.
:::
