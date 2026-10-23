---
title: Reputation channels
sidebar_position: 1
---

The bot only gives reputation in the channels you pick. A new server has no channels set, so
reputation by thankword or reaction works nowhere until you add some. Reputation given with
`/reputation` is the exception: it works in every channel.

You manage the list on the **Channels** page of the [dashboard](../getting-started/dashboard.md)
settings, or with the `/channel` command.

## Channels and categories

You can add single channels and whole categories.

- **Channels**: text channels, forum channels and voice channels. Threads and forum posts follow
  the channel they belong to.
- **Categories**: adding a category covers every channel inside it. The bot doesn't add the single
  channels, it remembers the category. A channel you later move into the category is covered, and a
  channel you move out of it is no longer covered.

In the dashboard you only see channels where the bot can read and write.

:::info
Without the supporter feature, a server can only add a small number of channels and categories.
The dashboard shows the limit when you reach it. See [Supporter features](../supporter.md).
:::

## Allow list or deny list

The list works in one of two modes:

| Mode | Command value | What it does |
|---|---|---|
| Allow list | `whitelist` | Reputation works only in the listed channels and categories. This is the default. |
| Deny list | `blacklist` | Reputation works everywhere except in the listed channels and categories. |

A deny list is handy when you have many channels and only want to leave out a few. The deny list is a
[supporter feature](../supporter.md).

## Commands

All `/channel` commands need the Administrator permission by default.

| Command | What it does |
|---|---|
| `/channel add <channel>` | Adds a channel or category to the list. |
| `/channel remove <channel>` | Removes a channel or category from the list. |
| `/channel set <channel>` | Replaces the list with this one entry. A channel replaces all listed channels, a category replaces all listed categories. |
| `/channel list` | Shows the listed channels and categories. |
| `/channel listtype <type>` | Switches between `whitelist` (allow list) and `blacklist` (deny list). |

The other `/channel` groups set the
[level-up announcement channel](./announcements.md#level-up-announcements), the
[autopost channel](./announcements.md#autopost), the [log channel](./logs-and-diagnostics.md#log-channel)
and the [system channel](./logs-and-diagnostics.md#system-channel).

## When reputation doesn't work in a channel

- Check that the channel or its category is on the list, and that the list mode is the one you want.
- Check that the bot can see the channel and read its message history.
- The **Problems** page of the dashboard lists channels and categories that no longer exist. See
  [Logs and diagnostics](./logs-and-diagnostics.md#problems-page).
