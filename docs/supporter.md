---
title: Supporter features
sidebar_position: 5
---

The Reputation Bot is free to use. On the public instance, some extra features are reserved for supporters. They
help pay for hosting and development. Supporter features are unlocked per server, not per member.

## What supporting unlocks

| Feature | What it gives your server |
|---|---|
| Full Reputation Log | All pages of `/log received` and `/log donated`, and all pages of the rankings in the dashboard. |
| Extended Analyzer Log | The bot keeps the analyzer log, which explains why a message gave reputation or not, for longer. |
| Unlimited Reputation Channel | More than the free number of reputation channels. The dashboard shows the free limit. |
| Unlimited Reputation Categories | More than the free number of reputation categories. |
| Channel Denylist | Pick the channels where the bot is **not** active, instead of the ones where it is. |
| Locale Overrides | Your own name for "reputation", like "Karma" or "Points". |
| Autopost | Posts the ranking into a channel on a schedule. |
| Advanced Rankings | The given ranking and the per member rankings of `/ranking` and the dashboard. |
| Detailed Profile | More information on profiles, with `/profile` and its `detailed` option. |
| Log Channel | A channel that lists every reputation given and removed. |
| Additional Emojis | More than one emoji that gives reputation as a reaction. |
| Bot Profile | Your own nickname and profile picture for the bot on your server. |
| Integration Bypass | Lets other bots and integrations give reputation. |
| Custom command names | Your own names for `/reputation` and `/profile`. See [custom command names](./reputation/profiles-and-rankings.md#custom-command-names). |

When you try a supporter feature without access, the bot tells you which supporter tiers include it, and shows
buttons to get one.

## Ways to support

There are two ways to unlock features: buy a supporter tier, or earn tokens by voting for the bot.

### Supporter tiers

A supporter tier unlocks a set of features. You can get one as a subscription or as a lifetime purchase.

**On Discord.** Buy a tier with the buttons the bot shows. `/supporter info` shows them too. A lifetime
purchase on Discord belongs to you until you use it. Activate it on a server with `/supporter activate <coupon>`.
The `coupon` option suggests the lifetime purchases you can use.

**On Ko-fi.** Buy a tier on [Ko-fi](https://ko-fi.com/reputationbot), then open **Supporter** in the
[dashboard](./getting-started/dashboard.md) menu. It lists your Ko-fi subscriptions and lifetime purchases. Use
**Assign** to unlock the features on one of your servers, and **Unassign** to move them.

The bot finds your Ko-fi purchases by your email address. It knows the email of your Discord account. If you used
another address on Ko-fi, add it in your **User Settings** and confirm it with the link the bot sends you.

These commands help with supporter tiers. They are available to administrators by default.

| Command | What it does |
|---|---|
| `/supporter info` | Shows the supporter tiers of your server, your unused lifetime purchases, and buttons to buy a tier. |
| `/supporter activate <coupon>` | Uses one of your Discord lifetime purchases on this server. |
| `/supporter refresh` | Loads your server's Discord purchases again, if something isn't working as expected. |

### Voting and tokens

Vote for the bot on bot lists to earn tokens, and spend them on single features in the **Token Shop**.

1. Use `/vote`, or open **Voting** in the dashboard menu. It lists the bot lists and when you last voted on each.
2. Each vote gives you a token. Voting regularly builds a streak, which gives an extra token. Voting on every bot
   list within 6 hours gives a daily bonus.
3. Your tokens are personal. To give them to a server, transfer them on the **Voting** page, or pick a **Vote
   Guild** in your **User Settings**. Future votes then go to that server.
4. Open **Token Shop** in the dashboard and buy a feature for the server you have selected.

A feature bought with tokens runs for 30 days. Buying it again while it is active adds another 30 days. With **Auto
Renewal** on, the bot renews it from the server's tokens when it runs out. If that fails, it says so in your system
channel.

You can pay with your personal tokens or with the server's tokens. Only members who manage the server can spend the
server's tokens. Whether other members can buy features with their personal tokens is set by **Everyone Token
Purchase** under **Settings > General**. It is on by default. With it off, only members who manage the server can
buy features.

If your server already has a feature through a supporter tier, you can't buy it with tokens.

## Running your own bot

If you host the bot yourself, you can unlock every supporter feature on all servers with `BOT_GRANT_ALL_SKU=true`.
See [self-hosting configuration](./self-hosting/configuration.md).
