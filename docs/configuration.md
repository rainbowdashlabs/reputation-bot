# Configuration and Customization

We compiled a list of configuration and customization possibilities on this page. These settings are not covered by 
our [setup command](./setup.md) or on the [abuse protection page](./abuse_protection.md)


## Reputation Roles
Reputation roles offer you a way to reward your users for reaching a reputation milestone.

You can add those roles via the `roles` command. Be aware that every role can only have one reputation amount 
associated and every reputation amount can only have one role.

On default the user will only have the highest possible role assigned to him. You can change this to role stacking, 
which would give the user all roles which are below his current reputation amount as well. This can be done via the 
`roles` command as well.

## Reputation Channel and categories
When it comes to defining channels where reputation can be collected you have multiple options. All these options 
can be found under the `channel` command

You can add channels separately or you can add categories as well. Adding a category will enable the bot on all 
channels in this category. Note that no indivdual channels will be added. So removing or adding channels of this 
category will enable or disable reputation for these channels.

If you have a lot of channels and you want to only exclude a few of these you can set the list mode to blacklist. 
This will disable reputation in all defined channels.

## Level Up Messages
The bot can send level up messages when a user received a higher reputation role.

These messages can be send in the channel where the user received their reputation or in another dedicated channel. 
You can define these settings via the `channel announcement` command.

## Managing ways to give reputation
The bot has several ways to give reputation. The active ways can be managed via the `repsettings`. You can find 
additional information on the [giving reputation page](./give_reputation.md)

### Mention
When the bot detects a thankword and there is a mention in the message, the mentioned user or users will receive 
reputation.

### Answer
When the bot detects a thankword in a message which is an answer to another message, the author of the referenced 
message will receive a reputation.

### Reaction
When a user reacts to a message with any registered reputation emote, the author of the message will receive a 
reputation. Note that when the reacted message gave reputation to another user, the receiver of this reputation will 
get more reputation instead.

### Fuzzy
When the bot detects a thankword in a message which is not an answer and has no mentions it will check the words 
around the thankword for user names. If it finds a matching name which is similar enough to a user name this user 
will receive reputation. 

### Confirmation Message
If the bot detects a thankword, but can't detect any obvious receiver it will ask the user via an embed. This embed 
will contain all users which might be eligible for reputation because they showed recent activity in the channel 
after the thanker wrote its first message.

Sometimes this embed will only contain one entry. In this case reputation can be given directly when enabling the 
`skip single target embed` option.


## Reputation Mode
The reputation mode decides how the bot calculates the rankings and roles. You can set it in your `repsettings`.
Currently we offer three different modes:

- Total: The total all time reputation of users
- 30 Days: The reputation collected in the last 30 days
- 7 Days: The reputation collected in the last 7 days

## Modifying Reputation
When using the reputation mode `total` you can give, take and set reputation via the repadmin command.

Note that an offset is not supported on any time based reputation mode.
