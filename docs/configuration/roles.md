---
title: Roles
sidebar_position: 3
---

Roles do two jobs in the bot. Reputation roles reward users who reach a reputation milestone.
Donor and receiver roles decide who may give and receive reputation at all.

## Reputation roles

A reputation role, called a rank in the dashboard, is a role paired with an amount of reputation.
When a user reaches the amount, the bot gives them the role.

You manage reputation roles in the [dashboard](../getting-started/dashboard.md) under
**Settings > Ranks**:

1. Under **Add New Rank**, pick a role and enter the **Required Reputation** (0 to 100,000).
2. Click **Confirm**.

Your ranks are listed below, each with buttons to edit its amount or delete it.

- Each role can only be used once.
- Each amount can only be used by one role.
- You can only pick roles that sit below the bot's own highest role and that are not managed by an
  integration. To use a higher role, drag the bot's own role above it in **Server Settings > Roles**.
  Discord doesn't let a bot give a role that sits above its own.

The amount the bot compares against depends on your
[reputation mode](../reputation/reputation-modes.md). In a mode that only counts the last 7 days, a
user can lose a role again.

### Stacking roles

By default a user only has the highest reputation role they reached. Turn on
**Settings > Ranks > Stack Roles**, and users keep every reputation role they reached.

### How the bot manages the roles

The bot only touches roles that are registered as reputation roles. When it updates a user, it
removes every reputation role the user shouldn't have and adds the ones they should have.

The bot updates a user's roles when:

- the user receives reputation,
- an admin changes their reputation with `/repadmin`,
- someone looks at their profile with `/profile`,
- the user writes a message, in every reputation mode except **Total Reputation**. This happens at
  most every 30 minutes per user,
- a new week or month starts. Shortly after 01:00 UTC on Mondays in the **This Week** mode, and on
  the first day of the month in the **This Month** mode, the bot updates every user who holds a
  reputation role.

So after you add a new role, users don't get it right away. To update everyone at once, click
**Refresh Ranks** under **Settings > Ranks**. A refresh checks every user with reputation on your
server. Only one refresh can run at a time.

If the bot can't give or take a role, it posts "Can not interact with role ... Please adjust the
hierarchy." in the channel. The **Problems** tab of the dashboard also lists reputation roles the
bot can't assign or that no longer exist.

## Who can give and receive reputation

Under **Settings > Roles** you can limit who takes part:

| Setting | What it does |
|---|---|
| Donor Roles | Only users with at least one of these roles can give reputation. |
| Receiver Roles | Only users with at least one of these roles can receive reputation. |
| Deny Donor Roles | Users with any of these roles can't give reputation. |
| Deny Receiver Roles | Users with any of these roles can't receive reputation. |

An empty donor or receiver list means everyone may give or receive.
