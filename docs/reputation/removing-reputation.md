---
title: Removing reputation
sidebar_position: 4
---

Reputation can disappear in a few ways: when its message is deleted, when your team changes it by hand, or when a
member leaves your server.

## Deleting messages and reactions

Every reputation is tied to a message, and deleting that message removes it.

- Reputation from an answer, a mention, fuzzy matching or the embed is tied to the thank you message. Delete the thank
  you and the reputation is gone.
- Reputation from a reaction is tied to the message that got the reaction. Delete that message and the reputation is
  gone.
- Removing a reputation reaction takes back the reputation it gave.

This also works when many messages are deleted at once. Reputation given with the `/reputation` command has no
message and stays.

:::warning
Removing a single reaction only takes reputation back while the reaction message is on, under
**Settings > Reputation > Reputation Messages > Reaction Reputation** (it is on by default). When it does, or when a moderator removes all reactions of a message, the bot removes every reputation
tied to that message, not only the one from the reaction.
:::

To find out which reputation a message gave, use `/log message <messageid>` or right click the message and pick
**Apps > Message Log**. See [logs and diagnostics](../configuration/logs-and-diagnostics.md).

## Changing reputation by hand

Use `/repadmin reputation` to correct a member's reputation:

| Command | What it does |
|---|---|
| `/repadmin reputation add <user> <amount>` | Adds 1 to 1,000,000 reputation. |
| `/repadmin reputation remove <user> <amount>` | Removes 1 to 1,000,000 reputation. |
| `/repadmin reputation set <user> <amount>` | Sets the reputation to a value from 0 to 1,000,000. |

The bot doesn't change the original reputation. It stores an offset that is added on top. `/repadmin profile <user>`
shows the raw reputation, the offset and the given reputation of a member. After a change, the bot updates the
member's rank roles.

This works best with the **Total Reputation** [mode](./reputation-modes.md). In the other modes, the offset counts
from the moment you made the change and leaves the time frame like any other reputation. A member can then end up
below zero for a while, for example when the reputation you removed by hand is still inside the time frame but the
reputation it cancelled out is not.

`/repadmin` is available to administrators by default. See
[who can manage the bot](../getting-started/first-setup.md#who-can-manage-the-bot).

## Members who leave

When a member leaves your server, the bot schedules their data on that server for removal 14 days later. If they
come back within that time, nothing is removed.

While the removal is pending, the member no longer shows up in rankings. Once it runs, the bot deletes the reputation
they received and any offsets. Reputation they gave stays with the members who received it, but is no longer linked
to them.

## Prune

`/prune` removes members' data right away, without the 14 day wait. It is available to members with **Manage
Messages** by default.

| Command | What it does |
|---|---|
| `/prune user <user>` | Removes the data of a member on your server. |
| `/prune user <userid>` | The same for someone who already left. Paste their user ID. |
| `/prune guild` | Checks everyone with reputation on your server and removes those who are no longer members. |

Use either `user` or `userid` with `/prune user`. Pruning removes the same data as the automatic removal above. It
can't be undone.

You rarely need `/prune guild`, because the bot removes members who leave on its own.

## Removing your own data

Members can ask for their data to be deleted from every server with `/gdpr delete`. See [privacy](../privacy.md).
