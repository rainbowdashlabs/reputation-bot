---
title: Roles
sidebar_position: 3
---

Roles do two jobs in the bot. Reputation roles reward users who reach a reputation milestone.
Donor and receiver roles decide who may give and receive reputation at all.

## Reputation roles

A reputation role, called a rank in the dashboard, is a role paired with an amount of reputation.
When a user reaches the amount, the bot gives them the role.

You manage reputation roles on the **Ranks** page of the [dashboard](../getting-started/dashboard.md)
settings, or with the `/roles` command.

- Each role can only be used once. Adding a role again with another amount changes its amount.
- Each amount should only be used by one role. The dashboard doesn't let you pick an amount twice.
- The bot must be able to manage the role. Drag the bot's own role above every reputation role in
  **Server Settings > Roles**. Discord doesn't let a bot give a role that sits above its own.
- You can't use the `@everyone` role. In the dashboard you can't pick roles managed by an
  integration either.

`/roles list` shows your reputation roles and their amounts:

![The answer of /roles list, showing Beginner at 1 and Pro at 10 reputation](./roles.png)

The amount the bot compares against depends on your
[reputation mode](../reputation/reputation-modes.md). In a mode that only counts the last 7 days, a
user can lose a role again.

### Stacking roles

By default a user only has the highest reputation role they reached. Turn on **Stack Roles** on the
Ranks page, or run `/roles stackroles` with `stack` set to true, and users keep every reputation role they
reached.

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

So after you add a new role, users don't get it right away. To update everyone at once, run
`/roles refresh` or press **Refresh Ranks** on the Ranks page. A refresh checks every user with
reputation on your server. Only one refresh can run at a time.

If the bot can't give or take a role, it posts "Can not interact with role ... Please adjust the
hierarchy." in the channel. The **Problems** page of the dashboard also lists reputation roles the
bot can't assign or that no longer exist.

## Who can give and receive reputation

On the **Roles** page of the dashboard settings you can limit who takes part:

| Setting | What it does |
|---|---|
| Donor Roles | Only users with at least one of these roles can give reputation. |
| Receiver Roles | Only users with at least one of these roles can receive reputation. |
| Deny Donor Roles | Users with any of these roles can't give reputation. |
| Deny Receiver Roles | Users with any of these roles can't receive reputation. |

An empty donor or receiver list means everyone may give or receive. Donor and receiver roles can
also be set with `/roles donor` and `/roles receiver`. Deny roles are only available in the
dashboard.

## Commands

All `/roles` commands need the Manage Roles permission by default.

| Command | What it does |
|---|---|
| `/roles add <role> <reputation>` | Adds a reputation role, or changes the amount of an existing one. |
| `/roles remove <role>` | Removes a reputation role. |
| `/roles list` | Lists the reputation roles. |
| `/roles stackroles [stack]` | Turns role stacking on or off. Without `stack` it shows the current state. |
| `/roles refresh` | Updates the reputation roles of all users with reputation. |
| `/roles donor add <role>` | Adds a donor role. |
| `/roles donor remove <role>` | Removes a donor role. |
| `/roles receiver add <role>` | Adds a receiver role. |
| `/roles receiver remove <role>` | Removes a receiver role. |

After `/roles add`, `/roles remove` and `/roles stackroles`, the bot offers a **Refresh roles** button
that starts a refresh.
