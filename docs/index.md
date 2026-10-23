---
title: Reputation Bot
sidebar_position: 0
---

Reputation Bot finds out who helped whom on your Discord server. When someone thanks another member, the bot gives
that member reputation. Reputation feeds rankings, profiles and reputation roles.

Most level systems count chat activity. A member who answers questions all day ends up on the same level as a member
who asks them all day. Reputation Bot counts help instead, and it checks every thank you so that reputation goes to
people who were really part of the conversation.

## How it works

You pick a few thankwords, like "thanks" or "thx", and the channels where reputation counts. When a member writes a
thankword, the bot looks for the person they mean: someone they mention, the author of the message they reply to, or a
name close to the thankword. If it is not sure, it asks the member with a short message where they pick the person.
Members can also give reputation with a reaction.

You don't need a command to thank someone. People write "thanks" anyway, and the bot cleans up its own messages
afterwards. The ways to give reputation are described on [Giving reputation](./reputation/giving-reputation.md), and
the checks that keep it fair on [Abuse protection](./configuration/abuse-protection.md).

## Where to start

- **Use the public bot**: [invite it](./getting-started/invite.md), run the
  [first setup](./getting-started/first-setup.md) and fine-tune everything in the
  [dashboard](./getting-started/dashboard.md).
- **Host your own instance**: start with [Self-hosting](./self-hosting/index.md).

Every command is listed on [Commands](./commands.md). Questions are answered in the [FAQ](./faq.md).

## Help and links

- Support: [Discord server](https://discord.gg/5DrGmz7pHj)
- Public instance and dashboard: [repbot.chojo.dev](https://repbot.chojo.dev), usage statistics on
  [repbot.chojo.dev/metrics](https://repbot.chojo.dev/metrics)
- Terms and privacy of the public instance: [Terms of Service](https://repbot.chojo.dev/tos),
  [Privacy Policy](https://repbot.chojo.dev/privacy)
- Source: [GitHub](https://github.com/rainbowdashlabs/reputation-bot), licensed under the AGPL-3.0
