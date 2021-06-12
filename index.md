# Reputation Bot - Your community driven Level System

The Reputation Bot was created for communities where the main focus is on mutual help between users.

Normal level systems, which are based on pure activity are not able to reflect how much a user contributes to the
community. User which have a lot of knowledge have the same level as users which asks a lot of questions all the time.

The reputation bot changes this and allows users to give each other reputation when they say something like "Thank you".

The bot will then try to determine the receiver of the thank phrase or ask the user directly:

![Image of a text with a confirmation request.](https://chojos.lewds.de/vOz0UrEc6t.png)

Alternatively users can also use a custom emote to thank a user.

![Image of a reaction on a message](https://chojos.lewds.de/9VJzOVuIr3.png)

Another method is to answer on the message with a thank phrase.

![Image of an answer on a message](https://chojos.lewds.de/VvTRamr6Il.png)


You can also mention the user instead. This can be done via mention or with some kind of fuzzy matching.

![Image of fuzzy matching result](https://chojos.lewds.de/jp05ifXGet.png)

All options to give reputation can be disabled in the settings.

# Abuse protection

The whole system is designed to be as abuse proof as possible.

## Backthanking

If a user A received reputation from user B, neither A nor B can thank each other for a fixed time defined as cooldown.\
User B can still thank user C or any other user. Cooldown is measured between users and not global.

## Outdated Messages

Users can't thank other users on outdated messages which are older than a time defined by `maxmessageage`.

## Ghost Reputation

If a user thanks someone and deleted its messages the reputation will be removed as well.

## Context sensitive

User A can only thank user B when B has at least one message after the first message of user A in the channel. This also
takes the `maxmessageage` into account. User A can also thank B if they share or have shared a voice channel in
the `maxmessageage`.

# Setup

Setup is easy.

- Set your language with the /locale command.
- Define a manager role for people which can change the settings of the bot with /roles managerrole
- Add reputation roles which users will get when they reach a specific amount of reputation with /roles add
- Load the predefined thankwords for your language with `/thankwords loaddefault <language>`. You can load multiple
  defaults. You can also add own thankwords with `/thankwords add <thankword>`.
- Add channels where users can receive reputation with `/channel add`

Optional steps:

- Use the scan command to scan the messages in a channel for thank phrases. This will backfill your reputations. You can
  scan up to 100k messages in a channel. This will take some time, and you can only scan one channel at a time.
- Set your own reputation emote with `/repsettings reaction`. This can be a custom emote from your server, or a normal
  discord emote.
- Set the legacy prefix. All slash commands also exists as legacy text commands.
- Use the `/repsettings` command to tweak your settings.
- User the `/thankwords check` command to check if a message would give reputation.

# Commands

User commands are:

- `rep` - Shows the user reputation or reputation of another user.
- `top` - Shows the top users from this server.
- `info` - Shows various information about the bot.

Team commands are:

- `repsettings` - Manage your server settings
- `channel` - Manage reputation channel
- `roles` - Manage reputation and bot roles
- `locale` - Change the bot locale
- `log` - Get the recent donated or received reputation of a user. Or reputation information about a message.
- `scan` - Scan a channel for reputation messages.

# You can invite the bot with this [link](https://discord.com/oauth2/authorize?client_id=834843896579489794&scope=bot&permissions=1342532672).