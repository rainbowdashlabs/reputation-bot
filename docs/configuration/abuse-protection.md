---
title: Abuse protection
sidebar_position: 5
---

Abuse protection stops users from trading reputation or handing it out without a reason. Every
check below runs before reputation is given. If one fails, no reputation is given, and the
[analyzer log](./logs-and-diagnostics.md#analyzer-log) tells you which check it was.

You change these settings in the [dashboard](../getting-started/dashboard.md) under
**Settings > Abuse Protection**.

## Overview

| Setting in the dashboard | Default |
|---|---|
| [Cooldown (minutes)](#cooldown) | 30 minutes |
| [Cooldown Once](#cooldown) | off |
| [Cooldown Direction](#cooldown) | Bidirectional |
| [Max Message Age (minutes)](#max-message-age) | 30 minutes |
| [Min Messages](#min-messages) | 10 |
| [Donor must be present](#receiver-and-donor-context) | on |
| [Receiver must be present](#receiver-and-donor-context) | on |
| [Enable Given Limit](#donor-and-receiver-limits) | off |
| [Enable Received Limit](#donor-and-receiver-limits) | off |
| [Max Message Reputation](#max-message-reputation) | 3 |

Some checks always apply: nobody can give reputation to themselves, and bots can't give or receive
reputation. Bots can be allowed to give reputation with an
[integration bypass](#integration-bypass).

## Cooldown

After user A gave reputation to user B, the cooldown stops the same pair from doing it again too
soon. A can still give reputation to everyone else.

**Cooldown Direction** decides who the cooldown applies to:

- **Bidirectional** (default): A can't give B reputation, and B can't give A reputation, until the
  cooldown is over. This stops users from thanking each other back and forth.
- **Unidirectional**: only A can't give B reputation again. B can thank A right away.

Set **Cooldown (minutes)** from 0 to 525600 (one year), or use the quick buttons below the field.
`0` turns the cooldown off.

**Cooldown Once** makes the cooldown last forever: a user can give reputation to the same user only
once. Turning it on also sets the direction to unidirectional. Turning it off sets the cooldown back
to 30 minutes.

## Receiver and donor context

When a message contains a thankword, the bot builds a context: the list of users who were recently
active in the conversation. It reads up to the last 100 messages before the thank message.

A user is in the context when:

- they wrote a message after the first message the thanking user wrote within the
  [max message age](#max-message-age), or
- they wrote one of the last messages set by [min messages](#min-messages), not older than 12 hours,
- they are in a voice channel with the thanking user right now, or shared one within the
  [max message age](#max-message-age).

**Receiver must be present**: the receiver must be in the context. Without it, users can thank
anyone, even people who weren't part of the conversation.

**Donor must be present**: the donor must be in the context too. For reactions this means the user
who reacts must have been active in the channel recently.

Both are on by default. Turning them off makes reputation much easier to abuse.

## Max message age

Messages older than **Max Message Age (minutes)** are outdated. Reputation can't be given for an
outdated message, for example by answering it or reacting to it. The age also limits how far back
the bot looks when it builds the [context](#receiver-and-donor-context), also for
users who shared a voice channel.

Values go from 0 to 86400 minutes (60 days). `0` turns the age check off, and the context then covers all
messages the bot reads. The **Problems** tab of the dashboard warns you when the value is very low.

## Min messages

The authors of the last messages in the channel can always be thanked, even when their messages
are older than the max message age. **Min Messages** sets how many of the last messages count. They
must still be at most 12 hours old. You can also answer one of these messages to give reputation.

This helps when a user comes back to a conversation after a while. Values go from 0 to 100.

Min messages doesn't help reactions: a reaction on a message older than the max message age never
gives reputation.

## Max message reputation

**Max Message Reputation** is the most users that one message can give reputation to, by mentioning
them or through the confirmation embed. Values go from 0 to 50.

## Donor and receiver limits

The limits cap how much reputation one user can give or receive in a time window, for example 10
reputation in 24 hours.

- **Enable Given Limit**: caps how much reputation a user can give. Set the amount with **Max Given**.
- **Enable Received Limit**: caps how much reputation a user can receive. Set the amount with
  **Max Received**.

**Over Hours** is the time window. Amounts go from 1 to 1000, windows from 1 to 8760 hours (one
year). A limit you just turned on starts at 1 reputation in 1 hour, so set both values right after.

The limits only count reputation given and received on your server.

## Deleted messages

When a message that gave reputation is deleted, or a reputation reaction is removed, the
reputation is taken away again. See [Removing reputation](../reputation/removing-reputation.md).

## Integration bypass

Bots can't give reputation. With an integration bypass you allow a bot on your server to give
reputation anyway, for example a bot that relays messages from another platform. Integration bypass
is a [supporter feature](../supporter.md).

Open **Settings > Integration Bypass**, pick the bot under **Add Integration Bypass**, and choose
what it may do:

| Setting | What it does |
|---|---|
| Allow Reactions | The bot can give reputation with reactions. |
| Allow Answer | The bot can give reputation by answering a message. |
| Allow Mention | The bot can give reputation by mentioning a user. |
| Allow Fuzzy | The bot can give reputation by fuzzy matching. |
| Allow Direct | The bot can give reputation when only one receiver was found. The bot never gets a confirmation embed. |
| Ignore Cooldown | The [cooldown](#cooldown) doesn't apply to the bot. The [donor and receiver roles](./roles.md#who-can-give-and-receive-reputation) still do. |
| Ignore Limit | The [donor and receiver limits](#donor-and-receiver-limits) don't apply to reputation from the bot. |
| Ignore Context | The [context checks](#receiver-and-donor-context) don't apply to the bot. |

Bots can never receive reputation.
