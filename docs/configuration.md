# Configuration and Customization

We compiled a list of configuration and customization possibilities on this page. These settings are not covered by
our [setup command](setup.md) or on the [abuse protection page](abuse_protection.md)

## Reputation Roles

Reputation roles offer you a way to reward your users for reaching a reputation milestone.

You can add those roles via the `roles` command. Be aware that every role can only have one reputation amount
associated and every reputation amount can only have one role.

By default, the user will only have the highest possible role assigned to him. You can change this to role stacking,
which would give the user all roles which are below his current reputation amount as well. This can be done via the
`roles` command as well.

## Reputation Channel and categories

When it comes to defining channels where reputation can be collected you have multiple options. All these options
can be found under the `channel` command

You can add channels separately, or you can add categories as well. Adding a category will enable the bot on all
channels in this category. Note that no individual channels will be added. So removing or adding channels of this
category will enable or disable reputation for these channels.

If you have a lot of channels, and you want to only exclude a few of these you can set the list mode to blacklist.
This will disable reputation in all defined channels.

## Level Up Messages

The bot can send level up messages when a user received a higher reputation role.

These messages can be sent in the channel where the user received their reputation or in another dedicated channel.
You can define these settings via the `channel announcement` command.

## Managing ways to give reputation

The bot has several ways to give reputation. The active ways can be managed via the `repsettings`. You can find
additional information on the [giving reputation page](give_reputation.md)

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
around the thankword for usernames. If it finds a matching name which is similar enough to a username this user
will receive reputation.

### Confirmation Message

If the bot detects a thankword, but can't detect any obvious receiver it will ask the user via an embed. This embed
will contain all users which might be eligible for reputation because they showed recent activity in the channel
after the thanker wrote its first message.

Sometimes this embed will only contain one entry. In this case reputation can be given directly when enabling the
`skip single target embed` option.

## Reputation Mode

The reputation mode decides how the bot calculates the rankings and roles. You can set it in your `repsettings`.
Currently, we offer these modes:

- **Total**: The total **all-time** reputation of users
- **30 Days**: The reputation collected in the **last 30 days**
- **7 Days**: The reputation collected in the **last 7 days**
- **Week**: The reputation collected in the **current week** (Weeks start at midnight from sunday to monday UTC)
- **Month**: The reputation collected in the **current month**

The table below shows which reputation is summed up for each mode. Counted reputation is marked.

| 29    | 30    | 1     | 2     | 3     | 4     | 5     | 6     | 7     | 8     | 9     | 10    | 11    | 12    | 13    | 14    | 15    | 16    | 17    | 18    | 19    | 20    | 21    | 22    | 23    | 24    | 25    | 26    | 27    | 28    | 1     | 2     | 3     | Mode    | Reputation |         
|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|-------|---------|------------|
| Mo    | Tu    | We    | Th    | Fr    | Sa    | Su    | Mo    | Tu    | We    | Th    | Fr    | Sa    | Su    | Mo    | Tu    | We    | Th    | Fr    | Sa    | Su    | Mo    | Tu    | We    | Th    | Fr    | Sa    | Su    | Mo    | Tu    | We    | Th    | Fr    |         | Su         |
| **2** | **8** | **6** | **1** | **2** | **5** | **4** | **6** | **3** | **2** | **4** | **5** | **8** | **9** | **1** | **2** | **4** | **3** | **4** | **8** | **5** | **2** | **3** | **4** | **2** | **3** | **4** | **5** | **2** | **0** | **5** | **0** | **3** | Total   | 125        |
| 2     | 8     | 6     | **1** | **2** | **5** | **4** | **6** | **3** | **2** | **4** | **5** | **8** | **9** | **1** | **2** | **4** | **3** | **4** | **8** | **5** | **2** | **3** | **4** | **2** | **3** | **4** | **5** | **2** | **0** | **5** | **0** | **3** | 30 Days | 109        |
| 2     | 8     | 6     | 1     | 2     | 5     | 4     | 6     | 3     | 2     | 4     | 5     | 8     | 9     | 1     | 2     | 4     | 3     | 4     | 8     | 5     | 2     | 3     | 4     | 2     | 3     | **4** | **5** | **2** | **0** | **5** | **0** | **3** | 7 Days  | 19         |
| 2     | 8     | 6     | 1     | 2     | 5     | 4     | 6     | 3     | 2     | 4     | 5     | 8     | 9     | 1     | 2     | 4     | 3     | 4     | 8     | 5     | 2     | 3     | 4     | 2     | 3     | 4     | 5     | **2** | **0** | **5** | **0** | **3** | Week    | 10         |
| 2     | 8     | 6     | 1     | 2     | 5     | 4     | 6     | 3     | 2     | 4     | 5     | 8     | 9     | 1     | 2     | 4     | 3     | 4     | 8     | 5     | 2     | 3     | 4     | 2     | 3     | 4     | 5     | 2     | 0     | **5** | **0** | **3** | Month   | 8          |
                                                                                           
The chosen reputation mode will affect the rank and reputation amount shown in user profiles. It also determines the 
amount used to decide about the assigned reputation role. It also affects the ranking shown by default.

## Reset date

Instead of letting the bot determine your desired timeframe you can use a reset date. This can be set via the 
repadmin command.

Once you set a reset date, all reputation given before this date will be ignored by the bot. It is equal to delete 
the data without actually loosing it, in case you want to go back. You can only set one reset date. The date is 
ignored if it is set to a future date.


## Modifying Reputation

You can give, take and set reputation via the repadmin command. This will work best with the `total` reputation mode,
but time based modes are also supported. When using time based modes, note that there are moment where a user could
end up with a negative amount of reputation when the added reputation moves outside the timeframe but subtracted
reputation is inside the timeframe.

## Level Up Announcement

Use `channel announcement` command to define a channel where the user should send level up messages. It can be a 
fixed channel or the channel where the user received the last reputation.
