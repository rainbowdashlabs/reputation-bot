---
title: Abuse protection
sidebar_position: 5
---

Abuse protection stops users from trading reputation or handing it out without a reason. Every
check below runs before reputation is given. If one fails, no reputation is given, and the
[analyzer log](./logs-and-diagnostics.md#analyzer-log) tells you which check it was.

You change these settings on the **Abuse Protection** page of the
[dashboard](../getting-started/dashboard.md) settings, or with the `/abuseprotection` command.
`/abuseprotection info` shows the most important current values.

## Overview

| Setting | Dashboard | Command | Default |
|---|---|---|---|
| [Cooldown](#cooldown) | Cooldown (minutes) | `/abuseprotection cooldown set <minutes>` | 30 minutes |
| [Cooldown once](#cooldown) | Cooldown Once | `/abuseprotection cooldown once` | off |
| [Cooldown direction](#cooldown) | Cooldown Direction | `/abuseprotection cooldown direction <direction>` | bidirectional |
| [Receiver context](#receiver-and-donor-context) | Receiver must be present | `/abuseprotection context receiver [state]` | on |
| [Donor context](#receiver-and-donor-context) | Donor must be present | `/abuseprotection context donor [state]` | on |
| [Max message age](#max-message-age) | Max Message Age (minutes) | `/abuseprotection message age [minutes]` | 30 minutes |
| [Min messages](#min-messages) | Min Messages | `/abuseprotection message min [messages]` | 10 |
| [Max message reputation](#max-message-reputation) | Max Message Reputation | `/abuseprotection message reputation [amount]` | 3 |
| [Donor limit](#donor-and-receiver-limits) | Enable Given Limit | `/abuseprotection limit donor [limit] [hours]` | off |
| [Receiver limit](#donor-and-receiver-limits) | Enable Received Limit | `/abuseprotection limit receiver [limit] [hours]` | off |

A command without its value shows the current setting. All `/abuseprotection` commands need the
Administrator permission by default.

Some checks always apply: nobody can give reputation to themselves, and bots can't give or receive
reputation. Bots can be allowed to give reputation with an
[integration bypass](#integration-bypass).

## Cooldown

After user A gave reputation to user B, the cooldown stops the same pair from doing it again too
soon. A can still give reputation to everyone else.

The **direction** decides who the cooldown applies to:

- **Bidirectional** (default): A can't give B reputation, and B can't give A reputation, until the
  cooldown is over. This stops users from thanking each other back and forth.
- **Unidirectional**: only A can't give B reputation again. B can thank A right away.

Set the cooldown in minutes. `0` turns the cooldown off. The command accepts up to 10080 minutes
(one week), the dashboard up to 525600 minutes (one year).

**Cooldown once** makes the cooldown last forever: a user can give reputation to the same user only
once. In the dashboard, turning it on also sets the direction to unidirectional. The command
`/abuseprotection cooldown once` leaves the direction as it is. To leave this mode, set a normal
cooldown again.

`/abuseprotection cooldown get` shows the current cooldown and direction.

## Receiver and donor context

When a message contains a thankword, the bot builds a context: the list of users who were recently
active in the conversation. It reads up to the last 100 messages before the thank message.

A user is in the context when:

- they wrote a message after the first message the thanking user wrote within the
  [max message age](#max-message-age), or
- they wrote one of the last messages set by [min messages](#min-messages), not older than 12 hours,
- they are in a voice channel with the thanking user right now, or were recently.

**Receiver must be present** (`/abuseprotection context receiver`): the receiver must be in the
context. Without it, users can thank anyone, even people who weren't part of the conversation.

**Donor must be present** (`/abuseprotection context donor`): the donor must be in the context too.
For reactions this means the user who reacts must have been active in the channel recently.

Both are on by default. Turning them off makes reputation much easier to abuse.

## Max message age

Messages older than the max message age are outdated. Reputation can't be given for an outdated
message, for example by answering it or reacting to it. The age also limits how far back the bot
looks when it builds the [context](#receiver-and-donor-context).

`0` turns the age check off. The command accepts up to 10080 minutes (one week), the dashboard up to
86400 minutes (60 days). The **Problems** page of the dashboard warns you when the value is very
low.

## Min messages

The authors of the last messages in the channel can always be thanked, even when their messages
are older than the max message age. Min messages sets how many of the last messages count. They
must still be at most 12 hours old. You can also answer one of these messages to give reputation.

This helps when a user comes back to a conversation after a while. Values go from 0 to 100.

Min messages doesn't help reactions: a reaction on a message older than the max message age never
gives reputation.

## Max message reputation

The most users that one message can give reputation to, by mentioning them or through the
confirmation embed. The command accepts 1 to 49, the dashboard 0 to 50.

## Donor and receiver limits

The limits cap how much reputation one user can give or receive in a time window, for example 10
reputation in 24 hours.

- **Donor limit**: how much reputation a user can give.
- **Receiver limit**: how much reputation a user can receive.

With the command, `limit` is the amount and `hours` is the window. A limit of `0` turns it off. The
command accepts limits up to 100000 and windows from 1 to 168 hours. In the dashboard, turn the limit
on and set **Max Given** or **Max Received** (1 to 1000) and **Over Hours** (1 to 8760).

:::warning
The limits currently count reputation from every server the bot is on, not only from yours. A user
who is active on other servers with the bot reaches your limit sooner.
:::

## Deleted messages

When a message that gave reputation is deleted, or a reputation reaction is removed, the
reputation is taken away again. See [Removing reputation](../reputation/removing-reputation.md).

## Integration bypass

Bots can't give reputation. With an integration bypass you allow a bot on your server to give
reputation anyway, for example a bot that relays messages from another platform. Integration bypass
is a [supporter feature](../supporter.md) and only available on the **Integration Bypass** page of
the dashboard settings.

For each bot you choose what it may do:

| Setting | What it does |
|---|---|
| Allow Reactions | The bot can give reputation with reactions. |
| Allow Answer | The bot can give reputation by answering a message. |
| Allow Mention | The bot can give reputation by mentioning a user. |
| Allow Fuzzy | The bot can give reputation by fuzzy matching. |
| Allow Direct | The bot can give reputation when only one receiver was found. The bot never gets a confirmation embed. |
| Ignore Cooldown | The [cooldown](#cooldown) doesn't apply to the bot. |
| Ignore Limit | The [donor limit](#donor-and-receiver-limits) doesn't apply to the bot. |
| Ignore Context | The [context checks](#receiver-and-donor-context) don't apply to the bot. |

Bots can never receive reputation.

:::warning
**Ignore Cooldown** currently also skips the [donor and receiver role](./roles.md#who-can-give-and-receive-reputation)
checks for that bot. Users without a receiver role, or with a deny receiver role, can then receive
reputation from it.
:::
