---
title: The dashboard
sidebar_position: 3
---

The web dashboard at [repbot.chojo.dev](https://repbot.chojo.dev) is the place where you configure the bot. Every
setting of your server lives there. Members also use it to look at rankings and profiles.

## Opening the dashboard

There are two ways in:

- Go to [repbot.chojo.dev](https://repbot.chojo.dev) and click **Login with Discord**.
- Use `/settings` on your server. The bot answers with an **Open Session** button that only you can see. The link
  opens the dashboard with that server already selected.

`/setup` (button **Open Setup**) and `/diagnose` (button **Dashboard**) work the same way and take you straight to the
[setup](./first-setup.md#run-the-setup) or to the [Problems](#problems) page. These three commands are available to
members with the **Administrator** permission by default. You can change that in Discord under **Server Settings >
Integrations > Reputation Bot**.

You sign in with your Discord account. The bot asks Discord for three things:

- your user name and avatar,
- the list of servers you are on, to find the servers you share with the bot,
- your email address, to match purchases you made on [Ko-fi](../supporter.md).

The server you are working on is shown at the bottom right of every page, marked **Current Server**. Click it to
switch to another server. If a server is missing, for example because you just invited the bot, click the refresh
button (**Refresh server list**) in that menu. Your session is kept in the browser. Click **Logout** to end it.

## Who can change settings

The **Settings** are open to every member who has at least one of these permissions on your server:
Administrator, Manage Server, Manage Roles, Kick Members, Ban Members or Manage Channels. The Integrations settings in
Discord don't change this.

Everyone else can still sign in and see the rankings and profiles of the servers they are on.

## The menu

The bar at the top has three entries:

- **Dashboard**: what every member sees, see [below](#what-every-member-sees).
- **Token Shop**: spend vote tokens on supporter features, see [supporter features](../supporter.md).
- **Settings**: only shown to members who can change settings.

Click your name on the right for **User Settings**, **Voting**, **Supporter** and **Logout**. These pages are about your
own account and are explained on the [supporter](../supporter.md) page.

## What every member sees

Under **Dashboard** you find:

- **Overview**: total, weekly and today's reputation, the top channel and the top members.
- **Ranking**: the ranking of received or given reputation, in any [reputation mode](../reputation/reputation-modes.md).
- **Profile**: your own reputation profile, or the one of another member. See
  [profiles and rankings](../reputation/profiles-and-rankings.md).

## Settings

Open **Settings** in the top bar. Below it you see four tabs: **Settings**, **Presets**, **Audit Log** and **Problems**.
The **Settings** tab has a list of sections on the left. Most settings are saved as soon as you change them.

In these docs, a path like **Settings > Abuse Protection > Cooldown (minutes)** means: open **Settings**, pick the
**Abuse Protection** section, and look for the **Cooldown (minutes)** setting.

| Section | What it does |
|---|---|
| General | The bot's language on your server, the [system channel](../configuration/logs-and-diagnostics.md#system-channel), and whether every member can spend tokens on [supporter features](../supporter.md#voting-and-tokens). |
| Profile | The bot's nickname and profile picture on your server, and the name it uses for "reputation". [Supporter feature](../supporter.md). |
| Ranks | Roles members get at a certain amount of reputation. See [roles](../configuration/roles.md#reputation-roles). |
| Reputation | The [reputation mode](../reputation/reputation-modes.md), the ways to [give reputation](../reputation/giving-reputation.md) and the [reputation messages](../configuration/announcements.md#reputation-messages). |
| Channels | Where the bot is active. See [channels](../configuration/channels.md). |
| Roles | Who may give and who may receive reputation. See [roles](../configuration/roles.md#who-can-give-and-receive-reputation). |
| Reactions | The emojis that give reputation. See [thankwords and reactions](../configuration/thankwords-and-reactions.md#reactions). |
| Thankwords | The words that count as a thank you. See [thankwords and reactions](../configuration/thankwords-and-reactions.md#thankwords). |
| Abuse Protection | Cooldowns, limits, message age and context checks. See [abuse protection](../configuration/abuse-protection.md). |
| Announcements | Messages when a member reaches a new rank. See [announcements](../configuration/announcements.md#level-up-announcements). |
| Autopost | Posts the ranking into a channel on a schedule. See [autopost](../configuration/announcements.md#autopost). [Supporter feature](../supporter.md). |
| Log Channel | A channel that lists every reputation given and removed. See [log channel](../configuration/logs-and-diagnostics.md#log-channel). [Supporter feature](../supporter.md). |
| Integration Bypass | Lets other bots give reputation. See [integration bypass](../configuration/abuse-protection.md#integration-bypass). [Supporter feature](../supporter.md). |
| Command Names | Custom names for `/reputation` and `/profile`. See [custom command names](../reputation/profiles-and-rankings.md#custom-command-names). [Supporter feature](../supporter.md). |
| Scan | Searches older messages in your reputation channels for reputation. See [scanning old messages](../configuration/logs-and-diagnostics.md#scanning-old-messages). |

A supporter section shows a short note with the supporter tiers that unlock it when your server doesn't have it yet.

:::note
Many settings can still be changed with slash commands like `/channel`, `/roles` or `/abuseprotection`. These commands
are being phased out and tell you so when you use them. Use the dashboard instead.
:::

### Presets

**Presets** change a group of settings in one click. See [first setup](./first-setup.md#presets).

### Audit log

The **Audit Log** shows every settings change made in the dashboard: who changed which setting, when, and what it was
before and after. Changes made with slash commands are not listed.

### Problems

The **Problems** tab checks your setup and lists what stops the bot from working well. A red mark next to the tab
tells you that it found something. It looks for:

- permissions the bot is missing on the whole server, and what stops working because of it,
- permissions missing in single reputation channels or categories,
- a missing system channel, announcement channel, autopost channel or log channel,
- no thankwords or no reputation channels,
- rank roles that were deleted or sit above the bot's own role,
- reputation channels that were deleted or are not text channels,
- a very low maximum message age.

Each entry has a **Go to settings** link to the section where you fix it. `/diagnose` sends you a link to this page.
More on [logs and diagnostics](../configuration/logs-and-diagnostics.md).
