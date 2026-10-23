---
title: The dashboard
sidebar_position: 3
---

The web dashboard at [repbot.chojo.dev](https://repbot.chojo.dev) is the main place to configure the bot. Members
also use it to look at rankings and profiles.

## Signing in

You sign in with your Discord account. Click **Login with Discord** and confirm on Discord's page. The bot asks
Discord for three things:

- your user name and avatar,
- the list of servers you are on, to find the servers you share with the bot,
- your email address, to match purchases you made on [Ko-fi](../supporter.md).

Commands like `/settings`, `/setup`, `/scan` and `/diagnose` answer with a button. The link opens the dashboard for
the server you used the command on. If you are not signed in yet, you sign in first.

Use the server switcher at the top to change to another server. If a server is missing, for example because you just
invited the bot, use **Refresh server list**. Your session is kept in the browser. Click **Logout** to end it.

## What you can see

Every member of a server with the bot can open its **Dashboard**:

- **Overview**: total, weekly and today's reputation, the top channel and the top members.
- **Ranking**: the ranking of received or given reputation, in any [reputation mode](../reputation/reputation-modes.md).
- **Profile**: your own reputation profile, or the one of another member. See
  [profiles and rankings](../reputation/profiles-and-rankings.md).

The menu also has pages about your own account: **User Settings**, **Voting**, **Token Shop** and **Supporter**.
They are explained on the [supporter](../supporter.md) page.

## Settings

The **Settings** are only open to members who manage the server. See
[who can manage the bot](./first-setup.md#who-can-manage-the-bot).

| Section | What it does |
|---|---|
| General | The bot's language on your server, the system channel for important messages, and whether every member can spend tokens on supporter features. |
| Profile | The bot's nickname, profile picture and the name it uses for "reputation" on your server. Supporter feature. |
| Ranks | Roles members get at a certain amount of reputation, and whether they keep the lower ones. See [roles](../configuration/roles.md). |
| Reputation | The ways to give reputation, the [reputation mode](../reputation/reputation-modes.md), the reset date and the reputation messages. See [giving reputation](../reputation/giving-reputation.md) and [announcements](../configuration/announcements.md). |
| Channels | Where the bot is active, as an allow list or a deny list. See [channels](../configuration/channels.md). |
| Roles | Who may give and who may receive reputation. See [roles](../configuration/roles.md). |
| Reactions | The main reaction and additional reactions that give reputation. See [thank words and reactions](../configuration/thankwords-and-reactions.md). |
| Thankwords | The words that count as a thank you. See [thank words and reactions](../configuration/thankwords-and-reactions.md). |
| Abuse Protection | Cooldowns, limits, message age and context checks. See [abuse protection](../configuration/abuse-protection.md). |
| Announcements | Messages when a member reaches a new rank. See [announcements](../configuration/announcements.md). |
| Autopost | Posts the ranking into a channel on a schedule. Supporter feature. |
| Log Channel | A channel that lists every reputation given and removed. Supporter feature. See [logs and diagnostics](../configuration/logs-and-diagnostics.md). |
| Integration Bypass | Lets other bots give reputation. Supporter feature. |
| Command Names | Custom names for `/reputation` and `/profile`. Supporter feature. See [profiles and rankings](../reputation/profiles-and-rankings.md#custom-command-names). |
| Scan | Searches older messages in your reputation channels for reputation. |

Next to the settings, the bar at the top has three more pages: **Presets** (see [first setup](./first-setup.md#presets)),
**Audit Log** and **Problems**.

### Audit log

The audit log shows every settings change made in the dashboard: who changed which setting, when, and what it was
before and after. Changes made with slash commands are not listed.

### Problems

The problems page checks your setup and lists what stops the bot from working well:

- permissions the bot is missing on the whole server, and what stops working because of it,
- permissions missing in single reputation channels or categories,
- a missing system channel, announcement channel, autopost channel or log channel,
- no thank words or no reputation channels,
- rank roles that were deleted or sit above the bot's own role,
- reputation channels that were deleted or are not text channels,
- a very low maximum message age.

`/diagnose` sends you a link to this page. More about it on [logs and diagnostics](../configuration/logs-and-diagnostics.md).
