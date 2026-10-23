---
title: Reputation modes
sidebar_position: 3
---

The reputation mode decides which time frame counts. It sets the reputation and rank in profiles, the default
ranking of `/top`, and which [rank roles](../configuration/roles.md) members get. Reputation messages show the count
in this mode too.

You set the mode in the dashboard under **Settings > Reputation > Reputation Mode**. Changing it never deletes
reputation. It only changes what is counted.

## The modes

All times are in UTC.

| Mode in the dashboard | Option in commands | Counts reputation received since |
|---|---|---|
| Total Reputation | `total` | The beginning. |
| Rolling Week | `7 days` | The start of the day, 7 days ago. |
| Rolling Month | `30 days` | The start of the day, 30 days ago. |
| This Week | `week` | Monday, 00:00. |
| This Month | `month` | The first day of the month, 00:00. |

So Rolling Week covers the last 7 full days plus today so far, and Rolling Month the last 30 full days plus today.
"This Week" and "This Month" start at zero again every Monday and every first of the month.

## An example

Say today is Wednesday, 10 March 2027, and a member received this reputation:

| | Before 8 Feb | 8 Feb to 28 Feb | Mon 1 Mar to Tue 2 Mar | Wed 3 Mar to Sun 7 Mar | Mon 8 Mar to today | Reputation |
|---|---|---|---|---|---|---|
| Received | 20 | 30 | 4 | 10 | 6 | |
| Total Reputation | counted | counted | counted | counted | counted | **70** |
| Rolling Month | | counted | counted | counted | counted | **50** |
| This Month | | | counted | counted | counted | **20** |
| Rolling Week | | | | counted | counted | **16** |
| This Week | | | | | counted | **6** |

Rolling Month starts on 8 February, 30 days before 10 March. Rolling Week starts on 3 March, 7 days before.

## Roles in time based modes

In every mode except Total Reputation, members can lose reputation just because time passes. The bot updates their
rank roles when they write a message, at most every 30 minutes per member. On top of that, "This Week" updates all
rank roles every Monday and "This Month" on the first of the month, shortly after 01:00 UTC.

## Reset date

A reset date hides all reputation received before it. Use it to start fresh without deleting anything. Remove the
reset date and the old reputation counts again.

Set it in the dashboard under **Settings > Reputation > Reset Date**. Pick a date and time in your own time zone.
**Clear Reset Date** removes it. You can only have one reset date.

The **Reset Date** field only shows up while the mode is **Total Reputation**. A reset date stays in place when you
switch to another mode, and the bot then counts what is newer than both the reset date and the start of the mode's
time frame. To check or remove a reset date in another mode, switch to Total Reputation, change it, and switch back.

:::warning
A reset date in the future hides all reputation until that date has passed. After that, only reputation received
after the date counts. Until then, profiles and rankings show zero.
:::