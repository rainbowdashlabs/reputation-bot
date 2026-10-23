---
title: Privacy and your data
sidebar_position: 7
---

You can get a copy of the data the bot stores about you, and you can have it deleted. Both work
with the `/gdpr` command, on any server with the bot or in a direct message to the bot.

The terms of service and the privacy policy of the public bot are on its website:

- [Terms of service](https://repbot.chojo.dev/tos)
- [Privacy policy](https://repbot.chojo.dev/privacy)

They only apply to the public bot. A self-hosted bot is run by someone else, who sets their own
terms.

## Getting a copy of your data

Run `/gdpr request`. The bot sends you a JSON file with your data by direct message.

For this to work:

- you must share a server with the bot,
- you must allow direct messages from members of that server.

If the bot can't reach you, it tries again later, first after about an hour and then less often.
You can request your data once every 30 days.

The file contains:

- every reputation you gave or received, with the id of the other user,
- reputation an admin added to or removed from you,
- who you shared a voice channel with,
- pending deletion tasks and your past data requests,
- your mail addresses and Ko-fi purchases, if you linked any,
- your user settings, votes and vote tokens,
- the settings changes you made in the dashboard.

## Deleting your data

Run `/gdpr delete`. Your data is deleted within the next hours. You don't get a confirmation, and
you can't cancel the deletion. If you want to check, request your data again some time later.

If you have a supporter tier assigned to a server, the bot refuses. Remove the tier from the server
first.

What is deleted:

- all reputation you received, on every server,
- your user id on the reputation you gave. The reputation itself stays, because it belongs to the
  people who received it,
- reputation an admin added to or removed from you,
- your voice channel activity,
- your support threads,
- your mail addresses and Ko-fi purchases,
- your dashboard sessions and tokens,
- your user settings, votes and vote tokens,
- the settings changes you made in the dashboard. They disappear from the audit log of the servers
  where you made them.

The bot keeps the record of your data requests, so it can still limit them to one every 30 days.
## When you leave a server

When you leave a server, the bot removes your reputation on that server 14 days later. Your user
id is removed from the reputation you gave there. If you come back within the 14 days, nothing is
removed.

When the bot is removed from a server, the settings of that server are deleted 14 days later.
