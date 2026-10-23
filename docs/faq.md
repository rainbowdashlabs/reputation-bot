---
title: FAQ
sidebar_position: 6
---

## The bot doesn't react to thanks at all

A new server has no thankwords and no reputation channels. Add both:

- Load thankwords for your language, see [Thankwords](./configuration/thankwords-and-reactions.md#default-thankwords).
- Add the channels where reputation should work, see [Reputation channels](./configuration/channels.md).

Then run `/diagnose`. The [Problems page](./configuration/logs-and-diagnostics.md#problems-page)
lists missing permissions and other setup problems.

## Why can't I give reputation to a user?

There are several possible reasons:

- The user wasn't part of the conversation, so they aren't in the context of your message.
- You gave reputation to this user recently, or they gave reputation to you. The cooldown is
  still running.
- The message you answered or reacted to is too old.
- You or the user are missing a donor or receiver role, or have a role that is denied.
- The channel isn't a reputation channel.
- You or the user reached the limit of given or received reputation.

To thank a user, make sure that:

- the user wrote at least one message after one of yours,
- their message isn't too old (how old depends on the server settings),
- you haven't exchanged reputation with them shortly before.

You can also give reputation to users you share a voice channel with, or shared one with recently.

All of these checks are explained on [Abuse protection](./configuration/abuse-protection.md).

## What is a context?

When the bot finds a thankword, it builds a context: the users who were part of the conversation.
Only users in the context can receive reputation.

The bot reads the last messages of the channel. It looks for the oldest message the thanking user
wrote within the max message age (30 minutes by default), and takes every user who wrote after it.
It adds the authors of the last few messages (10 by default) as long as they are at most 12 hours
old, because people often come back to a conversation later. Finally, it adds the users who are or
were recently in a voice channel with the thanking user.

The details are on [Abuse protection](./configuration/abuse-protection.md#receiver-and-donor-context).

## Why did a user not receive reputation, or why was a message ignored?

There are many possible reasons, so the bot can tell you exactly. Run `/log analyzer <messageid>`,
or right-click the message and pick **Apps > Message Log**. It shows in detail how the bot handled
the message and which check stopped it. See
[Analyzer log](./configuration/logs-and-diagnostics.md#analyzer-log).

The bot no longer adds debug emojis to messages. The analyzer log replaces them.

## Which roles does the bot manage?

Only the roles registered as reputation roles. When the bot updates a user, it removes the
reputation roles the user shouldn't have and adds the ones they should have.

The bot doesn't update everyone right away when you add a new role. Use `/roles refresh` or
**Refresh Ranks** in the dashboard for that. See [Roles](./configuration/roles.md).

## How can I remove reputation from a user?

It depends on how the reputation was given. For a mention, an answer or fuzzy matching, delete the
message. For a reaction, remove the reaction. Admins can also change reputation with `/repadmin`.
See [Removing reputation](./reputation/removing-reputation.md).

## A user left the server. What happens to their reputation?

The bot removes the reputation of a user 14 days after they left the server. If they come back
within these 14 days, nothing is removed. While the removal is pending, the user doesn't show up in
the server ranking.

To remove the reputation right away, use `/prune user` for one user (with `userid` for a user who is
no longer on the server), or `/prune guild` for all users who are no longer on the server.

## How can I get or delete my data?

Use `/gdpr request` or `/gdpr delete`. See [Privacy and your data](./privacy.md).
