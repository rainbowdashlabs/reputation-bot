---
title: Discord application
sidebar_position: 2
---

One Discord application gives your bot two things: the bot account, and the "Login with Discord"
button of the dashboard.

## 1. Create the application

1. Open the [Discord Developer Portal](https://discord.com/developers/applications) and click
   **New Application**.
2. Give it a name. This is the name of your bot.

## 2. The bot account

1. Under **Bot**, click **Reset Token** and copy the token. You give it to the bot as `BOT_TOKEN`.
2. On the same page, under **Privileged Gateway Intents**, turn on:
   - **Server Members Intent.** The bot needs it to find members who were not mentioned directly.
   - **Message Content Intent.** The bot needs it to read messages for thank words.

The bot also uses message reactions and voice states, but those intents are not privileged. You
don't need the **Presence Intent**.

:::warning
Keep the token secret. Anyone with it can log in as your bot. If it leaks, reset it in the portal.
:::

## 3. Login for the dashboard

The dashboard signs people in with Discord. It asks for the scopes `identify`, `guilds` and
`email`.

1. Under **OAuth2**, copy the **Client ID**. Then click **Reset Secret** and copy the
   **Client Secret**.
2. Under **OAuth2 > Redirects**, add your dashboard address with `/v1/auth/discord/callback` at the
   end:

   ```text
   https://repbot.example.com/v1/auth/discord/callback
   ```

3. Give the three values to the bot:

   | Setting | Environment variable |
   |---|---|
   | `discordOAuth.clientId` | `BOT_DISCORD_CLIENT_ID` |
   | `discordOAuth.clientSecret` | `BOT_DISCORD_CLIENT_SECRET` |
   | `discordOAuth.redirectUri` | `BOT_DISCORD_REDIRECT_URI` |

The redirect URI in the bot must match the one in the portal character for character.

After the login, the bot sends people back to `api.url` (or `BOT_API_URL`). Set it to the same
public address, without a trailing slash. See [configuration](./configuration.md#api).

## 4. Invite your bot

Under **OAuth2 > URL Generator**, tick the scopes `bot` and `applications.commands`, then these
permissions:

| Permission | Needed for |
|---|---|
| View Channels | Seeing the channels where people thank each other |
| Send Messages, Send Messages in Threads | Confirmation messages |
| Embed Links | Messages with embeds |
| Read Message History | Embed confirmations and most abuse protection checks |
| Add Reactions, Use External Emojis | Confirmation with reactions and emojis |
| Manage Messages | Part of the public instance's invite as well |
| Manage Roles | Giving reputation roles |
| Change Nickname | Changing the bot's nickname in the dashboard |
| Manage Expressions | Suggesting custom reactions in the dashboard |
| Bypass Slowmode | Confirmation messages in channels with slowmode |

Open the generated link and pick your server. You can also build the link yourself:

```text
https://discord.com/oauth2/authorize?client_id=YOUR_CLIENT_ID&scope=bot+applications.commands&permissions=4503875914918976
```

Put the same link into `links.invite`. Otherwise `/invite` and `/info` show the invite of the public
instance. See [configuration](./configuration.md#links).

## 5. Bot owner and bot guild

Two settings in `baseSettings` tell the bot who runs it:

- **`botOwner`** is a list of Discord user ids. Bot owners can restart and stop the bot with
  `/bot`, and they can open the dashboard of every server the bot is in.
- **`botGuild`** is the id of your own server. The bot registers the `/bot` command only there.
  Set it before you start the bot for real.

To copy an id, turn on **Developer Mode** in Discord under **Settings > Advanced**. Then right-click
a server or a user and pick **Copy ID**.

See [managing the bot](./managing.md) for what `/bot` can do.
