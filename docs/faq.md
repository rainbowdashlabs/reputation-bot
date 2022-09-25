# Frequently asked questions

### What are those emojis and what do they mean?

Those emojis indicate what the bot does with a message:  

- 👀 ➜ Found a thankword
- 💤 ➜ Receiver was on cooldown
- 🔍 ➜ No receiver was found
- ❓ ➜ Receiver has no recent messages in this channel and is missing in the context
- ❔ ➜ Donor is not present in channel
- 🕛 ➜ The referenced message is too old
- 🗨️ ➜ User was prompted for reputation.

A bot admin can disable these emojis in the `repsettings`.

### Why cant I give reputation to user XY?

This can have several reasons:

- The user is not in the context for this message
- You gave reputation to this user recently
- This user gave reputation to you recently

To thank a user make sure that:

- The user has written at least one message after a message of you
- The message of the user you want to thank is not too old (How old depends on the server settings)
- You haven't received reputation from this user shortly

You can also give reputation to users you currently share or recently shared a voice channel with.

You can read more about this mechanic on our [abuse protection page](abuse_protection.md)

### What is a context?

In order to resolve targets of a message and check which users are qualified to receive reputation, we build a context.
This context contains all users which are qualified to receive reputation and is build for every message when the bot
detects a thankword.

If you are interested in how it works, here is a short explanation:  
To build the message context we search for the oldest message within a fixed time (This is the max message age. 30
minutes on default).  
After we found the older message of the reputation donor we collect all users which have written a message after the
oldest message. If we haven't found any message within the max message age we add the users of the last 10 messages (The
amount depends on the server settings) to the context. We do this because often users come back after some time and the
thank duration already ran out.  
Finally we add also the users which share a voice channel with the message author and the users which shared a voice
channel with the users withing the max message age.

You can read more about this mechanic on our [abuse protection page](abuse_protection.md)

### What roles will be managed by the bot?

The bot will manage all roles which are registered as reputation roles.  
The bot will always check the user roles when a user receives reputation. If you add a new role, the bot will adjust the
user roles, when these users receive reputation again.

When the bot updates user roles it will first remove all roles registered as reputation roles from the user.  
It will then add the new reputation role to the user.

### How can I remove reputation of a user?

How you can remove reputation depends on the way the reputation was given to the user.

**Mention, Answer, Fuzzy, Embed**  
Delete the message

**Added by Reaction**  
Delete the reaction of the user

### A user has left the server but still shows up in top command

The reputation of a user will be removed 14 days after the user has left the server.

The bot does not check if a user is still on the server when the toplist is retrieved. That is the reason why those
users are still in the top list.

You can remove all reputation of a single user with the `prune user <userid>` command.  
Alternatively you can remove reputation of all users which are no longer on this guild with the `prune guild` command.

### How can I request my data?

You have to be on a server with the Reputation Bot. If you don't have one, join the support server. Make also sure 
that your privacy settings are not blocking dms.

You can request a copy of your data with the `gdpr request` command.

You will receive a copy of all data as a json file. This file will contain all entries which are related with your user
id. User ids of other users are removed and will not be included in your data.

You will receive the data within a few hours.  
You can request your data every 30 days.

### How can I request deletion of my data?

You have to be on a server with the Reputation Bot. If you don't have one, join the support server.

You can request deletion of your data with the `gdpr delete` command.

Once you have submitted your request your data will be deleted within a few hours. You will not receive a confirmation
of the deletion. You can not abort the deletion of your data.  
You may request your data again after some time, if you want to check that your data was deleted.

We will delete all your received reputation on all servers.  
We will remove your user id on every given reputation by you, but we will not delete these reputations, since they don't
belong to you.
