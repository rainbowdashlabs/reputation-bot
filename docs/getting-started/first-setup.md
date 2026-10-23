---
title: First setup
sidebar_position: 2
---

Once the bot is on your server, a few minutes of setup get it working. Most of it happens in the web
[dashboard](./dashboard.md).

## Who can manage the bot

Two things decide who can change the bot's settings.

**Slash commands.** The commands that change settings, like `/setup`, `/settings` and `/repadmin`, are only
available to members with the **Administrator** permission at first. A few commands use other permissions:

| Command | Available by default to members with |
|---|---|
| `/roles` | Manage Roles |
| `/log`, `/prune` and the **Received Reputation**, **Given Reputation** and **Message Log** apps | Manage Messages |

To let other roles or members use a command, open **Server Settings > Integrations**, pick the Reputation Bot and
change the command's permissions there. This is Discord's own system, so it works like for any other bot.

**The dashboard.** The settings in the dashboard are open to every member who has at least one of these permissions
on your server: Administrator, Manage Server, Manage Roles, Kick Members, Ban Members or Manage Channels. The
Integrations settings do not change this. Everyone else on the server can still open the dashboard to see rankings
and profiles.

## Run the setup

1. Use `/setup`. The bot answers with a button that only you can see.
2. Click **Open Setup** and sign in with Discord.
3. Follow the steps. Each one explains itself, and you can go back at any time.

The setup walks you through these steps:

| Step | What you choose |
|---|---|
| Select Language | The language of the bot's messages on your server. |
| System Channel | A channel for important messages from the bot and its developers. Pick an internal channel. **Required.** |
| Reputation Types | The ways your members can give reputation. See [giving reputation](../reputation/giving-reputation.md). |
| Reputation Mode | The time frame that counts for rankings and roles. See [reputation modes](../reputation/reputation-modes.md). |
| Reputation Ranks | Roles members get at a certain amount of reputation. At least one is **required**. See [roles](../configuration/roles.md). |
| Thankwords | The words that count as a thank you. Loading the defaults for your language is a good start. |
| Channels & Categories | Where reputation can be given. At least one channel or category is **required**, unless you use a deny list. |
| Server Scan | Optionally search older messages in those channels for reputation. |
| Announcements | Whether the bot announces when a member reaches a new rank. |
| Cooldown Settings | How long the same members have to wait before thanking each other again. |
| Main Reaction | The emoji that gives reputation when members react with it. |
| Donor & Receiver Roles | Optionally limit who can give and who can receive reputation. |

After the last step, the setup takes you to the settings, or to the scan page if you started a scan.

## Presets

Presets change a group of settings in one click. Open the dashboard and go to **Presets**. Each preset only changes
the settings it names and leaves everything else alone.

| Preset | What it changes |
|---|---|
| Default Configuration | Turns on reaction, answer, mention, fuzzy and embed reputation. Turns off direct embed and command reputation. Sets a 30 minute bidirectional cooldown and turns on both context checks. Announces reputation given by reaction and deletes those announcements after 30 seconds. |
| Simple Reputation System | Turns off every reputation type except the `/reputation` command, and turns off both context checks. |
| Thank Only Once | Members can thank the same member only once. Sets the cooldown to "once" and the direction to unidirectional. |
| No Abuse Protection | Turns off both context checks, so members don't need to be part of the recent conversation. |

The cooldown and context checks are explained on the [abuse protection](../configuration/abuse-protection.md) page.

## Open the dashboard later

Use `/settings` to get a link to the dashboard for your server. The bot answers with an **Open Session** button that
only you can see. You can also go to [repbot.chojo.dev](https://repbot.chojo.dev) directly and sign in with Discord.

`/dashboard` is something else: it shows a short summary of your server in Discord, with total, weekly and today's
reputation, the top channel and the member with the most reputation.

## Next steps

- Check the **Problems** page in the dashboard. It lists missing permissions, missing channels and roles the bot
  cannot give. `/diagnose` links you straight to it.
- Run a scan if you skipped it. Open **Scan** in the settings, or use `/scan`. A scan reads up to 10,000 recent
  messages in each reputation channel and adds the reputation it finds.
- Fine tune your [channels](../configuration/channels.md), [thank words and reactions](../configuration/thankwords-and-reactions.md)
  and [abuse protection](../configuration/abuse-protection.md).
- Tell your members how to [give reputation](../reputation/giving-reputation.md).

:::tip
If you get stuck, send the bot a direct message. The developers read and answer them. You can also ask on our
[support Discord](https://discord.gg/5DrGmz7pHj).
:::
