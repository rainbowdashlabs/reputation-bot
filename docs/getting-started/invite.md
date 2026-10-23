---
title: Invite the bot
sidebar_position: 1
---

You can use the Reputation Bot in two ways. Invite the public instance that we run for you, or run your own copy of
the bot.

## The public instance

We host the public instance, keep it up to date and take care of backups. To add it to your server, open the invite
link and pick your server:

[Invite the Reputation Bot](https://invite.repbot.chojo.dev)

You need the **Manage Server** permission on a server to add a bot to it.

The bot adds its slash commands to your server. Right after joining, it posts a short welcome message in the first
channel it can write in. Continue with the [first setup](./first-setup.md).

## Permissions the bot asks for

The invite asks for these permissions. You can untick some of them on the invite screen, but the bot then loses the
feature that needs it.

| Permission | What the bot needs it for |
|---|---|
| View Channels | Seeing your reputation channels. Without it, no text based way to give reputation works. |
| Send Messages | Confirmation embeds, reputation messages and level up messages. |
| Send Messages in Threads | The same messages inside threads. |
| Read Message History | Reading the recent conversation. The confirmation embed and most abuse protection checks need it. |
| Add Reactions | Marking messages that gave reputation with your main reaction. |
| Use External Emojis | Using emojis from other servers in its messages and reactions. |
| Bypass Slowmode | Answering in channels with slowmode without being held back. |
| Manage Roles | Giving the [reputation roles](../configuration/roles.md) to your members. |
| Change Nickname | Setting the bot's nickname from the dashboard. |
| Manage Expressions | Suggesting your server's own emojis in the dashboard. |
| Send Voice Messages | Nothing. The bot never sends voice messages. |

The bot also checks for **Embed Links** in your reputation channels. The invite does not ask for it, so the bot only
has it when your server gives it to everyone or to the bot's role. Most servers allow it for everyone by default.

:::warning
In a channel where the bot is missing **Send Messages**, **Add Reactions** or **Embed Links**, it ignores thank
messages. If thanking does nothing in one channel, check these permissions first. The **Problems** page of the
[dashboard](./dashboard.md) lists missing permissions per channel.
:::

Discord only lets a bot give roles that sit below its own highest role. In **Server Settings > Roles**, drag the
bot's role above every role you want it to hand out.

## Privileged intents

The bot relies on two privileged gateway intents. The public instance has both turned on, so there is nothing for you
to do. If you run your own bot, you need to turn them on in the Discord Developer Portal.

- **Message Content Intent**: the bot reads messages to find thank words like "thanks".
- **Server Members Intent**: the bot finds members by name (for example for fuzzy matching) and notices when a member
  leaves your server.

Besides these, the bot listens to reactions, messages and voice states. Voice states let members who talked in a
voice channel together thank each other.

## Run your own bot

The Reputation Bot is open source under the AGPL-3.0. If you want full control over your data and settings, you can
host it yourself. You also get every supporter feature for free. See [self-hosting](../self-hosting/index.md).
