---
title: Reputation channels
sidebar_position: 1
---

The bot only gives reputation in the channels you pick. A new server has no channels set, so
reputation by thankword or reaction works nowhere until you add some. Reputation given with
`/reputation` is the exception: it works in every channel.

You manage the list in the [dashboard](../getting-started/dashboard.md) under **Settings > Channels**.

## Channels and categories

**Channels and Categories** shows your server's categories with their channels, and the channels
without a category. Tick what should be on the list:

- **Channels**: text channels, forum channels and voice channels. Threads and forum posts follow
  the channel they belong to.
- **Categories**: ticking a category covers every channel inside it. The bot doesn't add the single
  channels, it remembers the category. A channel you later move into the category is covered, and a
  channel you move out of it is no longer covered.

The dashboard only lists channels where the bot can read and write.

:::info
Without the supporter feature, a server can only add a small number of channels and categories.
The dashboard shows the limit when you reach it. See [Supporter features](../supporter.md).
:::

## Allow list or deny list

**List Type** decides how the list works:

| List Type | What it does |
|---|---|
| Allow List | Reputation works only in the listed channels and categories. This is the default. |
| Deny List | Reputation works everywhere except in the listed channels and categories. |

A deny list is handy when you have many channels and only want to leave out a few. The deny list is a
[supporter feature](../supporter.md).

## Other channels

Some features use a channel of their own. You set those on their own pages:

- the [level-up announcement channel](./announcements.md#level-up-announcements) under **Settings > Announcements**,
- the [autopost channel](./announcements.md#autopost) under **Settings > Autopost**,
- the [log channel](./logs-and-diagnostics.md#log-channel) under **Settings > Log Channel**,
- the [system channel](./logs-and-diagnostics.md#system-channel) under **Settings > General**.

## When reputation doesn't work in a channel

- Check that the channel or its category is on the list, and that the list type is the one you want.
- Check that the bot can see the channel and read its message history.
- The **Problems** tab of the dashboard lists channels and categories that no longer exist. See
  [Logs and diagnostics](./logs-and-diagnostics.md#problems-page).
