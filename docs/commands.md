---
title: Commands
sidebar_position: 4
---

The slash commands of the bot, with who can use them by default. `<option>` is required,
`[option]` is optional.

Server settings are not changed with commands but in the [dashboard](./getting-started/dashboard.md).
The old settings commands are listed at the [end of this page](#settings-commands), together with the
place in the dashboard that replaces them.

## Who can use a command

Each command comes with a default permission:

- **Everyone**: every member can use it.
- **Administrator**: only members with the Administrator permission see and use it.
- **Manage Messages**: members with that permission see and use it.

These are only defaults. You can change who may use each command in Discord under
**Server Settings > Integrations > Reputation Bot**.

Commands marked "server only" don't work in direct messages with the bot.

## Reputation and rankings

| Command | What it does | Who |
|---|---|---|
| `/reputation <user>` | Gives reputation to a user. Only exists on servers that turned on command reputation, see [Giving reputation](./reputation/giving-reputation.md#command). | Everyone |
| `/profile [user] [detailed]` | Shows your reputation profile, or the profile of another user. `detailed` is a [supporter feature](./supporter.md). | Everyone, server only |
| `/top [mode]` | Shows the ranking of received reputation on the server. | Everyone, server only |
| `/ranking server received [mode]` | Shows the ranking of received reputation on the server. | Everyone, server only |
| `/ranking server given [mode]` | Shows the ranking of given reputation on the server. [Supporter feature](./supporter.md). | Everyone, server only |
| `/ranking user received [mode] [user]` | Shows from whom a user received reputation. [Supporter feature](./supporter.md). | Everyone, server only |
| `/ranking user given [mode] [user]` | Shows to whom a user gave reputation. [Supporter feature](./supporter.md). | Everyone, server only |
| `/dashboard` | Shows reputation statistics of the server in Discord. | Everyone, server only |

`mode` is one of `total`, `7 days`, `30 days`, `week` and `month`. Without it, the server's
[reputation mode](./reputation/reputation-modes.md) is used. More on rankings on
[Profiles and rankings](./reputation/profiles-and-rankings.md).

With the command names [supporter feature](./supporter.md), you can give `/reputation` and
`/profile` other names on your server under **Settings > Command Names** in the dashboard.

## Opening the dashboard

| Command | What it does | Who |
|---|---|---|
| `/settings` | Gives you a link to the [dashboard](./getting-started/dashboard.md) for this server. | Administrator, server only |
| `/setup` | Gives you a link to the [setup](./getting-started/first-setup.md#run-the-setup) in the dashboard. | Administrator, server only |
| `/diagnose` | Gives you a link to the [Problems](./configuration/logs-and-diagnostics.md#problems-page) tab of the dashboard. | Administrator, server only |

## Managing reputation

| Command | What it does | Who |
|---|---|---|
| `/repadmin reputation add <user> <amount>` | Adds reputation to a user, 1 to 1,000,000. | Administrator, server only |
| `/repadmin reputation remove <user> <amount>` | Removes reputation from a user, 1 to 1,000,000. | Administrator, server only |
| `/repadmin reputation set <user> <amount>` | Sets the reputation of a user, 0 to 1,000,000. | Administrator, server only |
| `/repadmin profile <user>` | Shows the internal reputation profile of a user. | Administrator, server only |
| `/prune user [user] [userid]` | Removes all reputation of a user on the server. Use `userid` for users who left. | Manage Messages, server only |
| `/prune guild` | Removes the reputation of all users who are no longer on the server. | Manage Messages, server only |

More on this on [Removing reputation](./reputation/removing-reputation.md).

## Logs and checks

| Command | What it does | Who |
|---|---|---|
| `/log received <user>` | Shows the reputation a user received. | Manage Messages, server only |
| `/log donated <user>` | Shows the reputation a user gave. | Manage Messages, server only |
| `/log message <messageid>` | Shows the reputation given by a message. | Manage Messages, server only |
| `/log analyzer <messageid>` | Shows how the bot handled a message. | Manage Messages, server only |
| `/thankwords check <message>` | Checks whether a message in this channel would give reputation. See [testing a message](./configuration/thankwords-and-reactions.md#testing-a-message). | Administrator, server only |
| `/scan` | Starts a scan of old messages and links to its progress in the dashboard. | Administrator, server only |

See [Logs and diagnostics](./configuration/logs-and-diagnostics.md).

## Supporter and general

| Command | What it does | Who |
|---|---|---|
| `/supporter info` | Shows the supporter standing of your server. | Administrator, server only |
| `/supporter refresh` | Refreshes your supporter benefits when something doesn't work. | Administrator, server only |
| `/supporter activate <coupon>` | Activates a lifetime subscription with a coupon. | Administrator, server only |
| `/vote` | Shows links to vote for the bot and earn tokens. | Everyone |
| `/info` | Shows information about the bot. | Everyone |
| `/invite` | Shows the link to invite the bot. | Everyone |
| `/gdpr request` | Sends you a copy of your data. See [Privacy and your data](./privacy.md). | Everyone |
| `/gdpr delete` | Deletes your data. See [Privacy and your data](./privacy.md). | Everyone |

See [Supporter features](./supporter.md) for what supporters get.

## Apps menu

Right-click a message or a user and open **Apps** to find these actions. They need the Manage
Messages permission by default and work on servers only.

| Action | On | What it does |
|---|---|---|
| Message Log | Message | Shows the [analyzer log](./configuration/logs-and-diagnostics.md#analyzer-log) of the message. |
| Received Reputation | User | Shows the reputation the user received, like `/log received`. |
| Given Reputation | User | Shows the reputation the user gave, like `/log donated`. |

## Settings commands

The bot still has commands that change settings. They are being phased out and show a notice that
points you to the dashboard. Use the dashboard instead:

| Old command | In the dashboard |
|---|---|
| `/abuseprotection` | **Settings > Abuse Protection**, see [Abuse protection](./configuration/abuse-protection.md) |
| `/channel add`, `remove`, `set`, `list`, `listtype` | **Settings > Channels**, see [Reputation channels](./configuration/channels.md) |
| `/channel announcement` | **Settings > Announcements**, see [Level-up announcements](./configuration/announcements.md#level-up-announcements) |
| `/channel autopost` | **Settings > Autopost**, see [Autopost](./configuration/announcements.md#autopost) |
| `/channel log` | **Settings > Log Channel**, see [Log channel](./configuration/logs-and-diagnostics.md#log-channel) |
| `/channel systemchannel` | **Settings > General > System Channel**, see [System channel](./configuration/logs-and-diagnostics.md#system-channel) |
| `/locale` | **Settings > General > Bot Language** |
| `/messages states` | **Settings > Reputation > Reputation Messages**, see [Reputation messages](./configuration/announcements.md#reputation-messages) |
| `/reactions` | **Settings > Reactions**, see [Reactions](./configuration/thankwords-and-reactions.md#reactions) |
| `/repadmin resetdate` | **Settings > Reputation > Reset Date**, see [Reset date](./reputation/reputation-modes.md#reset-date) |
| `/repsettings general` | **Settings > Reputation**, see [Giving reputation](./reputation/giving-reputation.md) and [Reputation modes](./reputation/reputation-modes.md) |
| `/repsettings name` | **Settings > Profile > Reputation Name** |
| `/roles` | **Settings > Ranks** and **Settings > Roles**, see [Roles](./configuration/roles.md) |
| `/thankwords add`, `remove`, `list`, `loaddefault` | **Settings > Thankwords**, see [Thankwords](./configuration/thankwords-and-reactions.md#thankwords) |

## Bot owner command

Self-hosters have one more command, `/bot`. It only exists on the bot's own server and is described
on [Managing the bot](./self-hosting/managing.md).
