---
title: Commands
sidebar_position: 4
---

Every slash command of the bot, with who can use it by default. `<option>` is required,
`[option]` is optional.

## Who can use a command

Each command comes with a default permission:

- **Everyone**: every member can use it.
- **Administrator**: only members with the Administrator permission see and use it.
- **Manage Messages** or **Manage Roles**: members with that permission see and use it.

These are only defaults. You can change who may use each command in Discord under
**Server Settings > Integrations > Reputation Bot**.

Commands marked "server only" don't work in direct messages with the bot.

## Reputation and rankings

| Command | What it does | Who |
|---|---|---|
| `/reputation <user>` | Gives reputation to a user. Only exists on servers that turned on command reputation, see [Giving reputation](./reputation/giving-reputation.md). | Everyone |
| `/profile [user] [detailed]` | Shows your reputation profile, or the profile of another user. `detailed` is a [supporter feature](./supporter.md). | Everyone, server only |
| `/top [mode]` | Shows the ranking of received reputation on the server. | Everyone, server only |
| `/ranking server received [mode]` | Shows the ranking of received reputation on the server. | Everyone, server only |
| `/ranking server given [mode]` | Shows the ranking of given reputation on the server. [Supporter feature](./supporter.md). | Everyone, server only |
| `/ranking user received [mode] [user]` | Shows from whom a user received reputation. [Supporter feature](./supporter.md). | Everyone, server only |
| `/ranking user given [mode] [user]` | Shows to whom a user gave reputation. [Supporter feature](./supporter.md). | Everyone, server only |
| `/dashboard` | Shows reputation statistics of the server. | Everyone, server only |

`mode` is one of `total`, `7 days`, `30 days`, `week` and `month`. Without it, the server's
[reputation mode](./reputation/reputation-modes.md) is used. More on rankings on
[Profiles and rankings](./reputation/profiles-and-rankings.md).

With the command names [supporter feature](./supporter.md), you can give `/reputation` and
`/profile` other names on your server, on the **Command Names** page of the dashboard settings.

## Server setup

| Command | What it does | Who |
|---|---|---|
| `/setup` | Gives you a link to the setup in the dashboard. | Administrator, server only |
| `/settings` | Gives you a link to the settings in the dashboard. Don't share it. | Administrator, server only |
| `/diagnose` | Gives you a link to the [Problems page](./configuration/logs-and-diagnostics.md#problems-page) of the dashboard. | Administrator, server only |
| `/locale set <language>` | Sets the language of the bot on your server. | Administrator, server only |
| `/locale reset` | Resets the language to the server language. | Administrator, server only |
| `/locale list` | Lists the available languages. | Administrator, server only |
| `/repsettings general` | Shows and changes the ways of giving reputation and the reputation mode. | Administrator, server only |
| `/repsettings name set <name>` | Renames "reputation" in the bot's messages, 1 to 16 characters. [Supporter feature](./supporter.md). | Administrator, server only |
| `/repsettings name reset` | Resets the name of reputation. | Administrator, server only |
| `/messages states` | Turns [reputation messages](./configuration/announcements.md#reputation-messages) on and off. | Administrator, server only |

## Channels

All of these are Administrator, server only.

| Command | What it does |
|---|---|
| `/channel set <channel>` | Replaces the [reputation channels](./configuration/channels.md) or categories with this one. |
| `/channel add <channel>` | Adds a reputation channel or category. |
| `/channel remove <channel>` | Removes a reputation channel or category. |
| `/channel list` | Lists the reputation channels and categories. |
| `/channel listtype <type>` | Uses the list as allow list (`whitelist`) or deny list (`blacklist`). |
| `/channel announcement state <active>` | Turns [level-up announcements](./configuration/announcements.md#level-up-announcements) on or off. |
| `/channel announcement where <where>` | Sends level-up announcements to the `same channel` or a `custom channel`. |
| `/channel announcement channel <channel>` | Sets the channel for level-up announcements. |
| `/channel announcement info` | Shows the level-up announcement settings. |
| `/channel autopost enable <channel> [refreshtype] [refreshinterval]` | Turns on the [autopost](./configuration/announcements.md#autopost) leaderboard. [Supporter feature](./supporter.md). |
| `/channel autopost disable` | Turns off the autopost. |
| `/channel autopost info` | Shows the autopost settings. |
| `/channel autopost refresh` | Refreshes the autopost now. |
| `/channel log enable <channel>` | Turns on the [log channel](./configuration/logs-and-diagnostics.md#log-channel). [Supporter feature](./supporter.md). |
| `/channel log disable` | Turns off the log channel. |
| `/channel systemchannel enable <channel>` | Sets the [system channel](./configuration/logs-and-diagnostics.md#system-channel). |
| `/channel systemchannel disable` | Removes the system channel. |

## Thankwords and reactions

All of these are Administrator, server only. See
[Thankwords and reactions](./configuration/thankwords-and-reactions.md).

| Command | What it does |
|---|---|
| `/thankwords add <pattern>` | Adds a thankword. |
| `/thankwords remove <pattern>` | Removes a thankword. |
| `/thankwords list` | Lists the thankwords. |
| `/thankwords loaddefault [language]` | Adds the default thankwords of a language. Without a language it lists the languages. |
| `/thankwords check <message>` | Checks whether a message in this channel would give reputation. |
| `/reactions main <emote>` | Sets the main reputation reaction. |
| `/reactions add <emote>` | Adds an additional reputation reaction. [Supporter feature](./supporter.md). |
| `/reactions remove <emote>` | Removes an additional reputation reaction. |
| `/reactions info` | Shows the reputation reactions. |

## Roles

All of these are Manage Roles, server only. See [Roles](./configuration/roles.md).

| Command | What it does |
|---|---|
| `/roles add <role> <reputation>` | Adds a reputation role, or changes its amount. |
| `/roles remove <role>` | Removes a reputation role. |
| `/roles list` | Lists the reputation roles. |
| `/roles stackroles [stack]` | Turns role stacking on or off. |
| `/roles refresh` | Updates the reputation roles of all users. |
| `/roles donor add <role>` | Adds a role that may give reputation. |
| `/roles donor remove <role>` | Removes a donor role. |
| `/roles receiver add <role>` | Adds a role that may receive reputation. |
| `/roles receiver remove <role>` | Removes a receiver role. |

## Abuse protection

All of these are Administrator, server only. See [Abuse protection](./configuration/abuse-protection.md).
Commands with an optional value show the current setting when you leave the value out.

| Command | What it does |
|---|---|
| `/abuseprotection info` | Shows the current abuse protection settings. |
| `/abuseprotection cooldown set <minutes>` | Sets the cooldown between the same users, 0 to 10080 minutes. |
| `/abuseprotection cooldown get` | Shows the cooldown and its direction. |
| `/abuseprotection cooldown once` | Lets users give reputation to the same user only once. |
| `/abuseprotection cooldown direction <direction>` | Sets the cooldown direction, `unidirectional` or `bidirectional`. |
| `/abuseprotection context donor [state]` | Requires the donor to be part of the conversation. |
| `/abuseprotection context receiver [state]` | Requires the receiver to be part of the conversation. |
| `/abuseprotection message age [minutes]` | Sets the max message age, 0 to 10080 minutes. |
| `/abuseprotection message min [messages]` | Sets how many of the last messages can always be thanked, 0 to 100. |
| `/abuseprotection message reputation [amount]` | Sets how many users one message can give reputation to, 1 to 49. |
| `/abuseprotection limit donor [limit] [hours]` | Limits how much reputation a user can give in a time window. |
| `/abuseprotection limit receiver [limit] [hours]` | Limits how much reputation a user can receive in a time window. |

## Managing reputation

| Command | What it does | Who |
|---|---|---|
| `/repadmin reputation add <user> <amount>` | Adds reputation to a user, 1 to 1,000,000. | Administrator, server only |
| `/repadmin reputation remove <user> <amount>` | Removes reputation from a user, 1 to 1,000,000. | Administrator, server only |
| `/repadmin reputation set <user> <amount>` | Sets the reputation of a user, 0 to 1,000,000. | Administrator, server only |
| `/repadmin resetdate set <year> <month> <day>` | Ignores all reputation given before this date. | Administrator, server only |
| `/repadmin resetdate now` | Ignores all reputation given before now. | Administrator, server only |
| `/repadmin resetdate current` | Shows the current reset date. | Administrator, server only |
| `/repadmin resetdate remove` | Removes the reset date. | Administrator, server only |
| `/repadmin profile <user>` | Shows the internal reputation profile of a user. | Administrator, server only |
| `/prune user [user] [userid]` | Removes all reputation of a user on the server. Use `userid` for users who left. | Manage Messages, server only |
| `/prune guild` | Removes the reputation of all users who are no longer on the server. | Manage Messages, server only |

More on this on [Reputation modes](./reputation/reputation-modes.md) and
[Removing reputation](./reputation/removing-reputation.md).

## Logs and scans

| Command | What it does | Who |
|---|---|---|
| `/log received <user>` | Shows the reputation a user received. | Manage Messages, server only |
| `/log donated <user>` | Shows the reputation a user gave. | Manage Messages, server only |
| `/log message <messageid>` | Shows the reputation given by a message. | Manage Messages, server only |
| `/log analyzer <messageid>` | Shows how the bot handled a message. | Manage Messages, server only |
| `/scan` | Starts a scan of old messages and links to its progress. | Administrator, server only |

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

## Bot owner command

Self-hosters have one more command, `/bot`. It only exists on the bot's own server and is described
on [Managing the bot](./self-hosting/managing.md).
