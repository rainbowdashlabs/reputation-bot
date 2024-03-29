# Abuse Protection

The bot has several mechanics to prevent users from receiving or giving reputation without a reason.

The settings mentioned here can be modified via the `abuseprotection` command.

## Backthanking

Setting: `cooldown`

When User A thanks User B they get a cooldown for each other. During this cooldown, User B can not thank User A 
again and vice versa.

However, User A can thank any other user during this time, as well as User B.

## Receiver and Donor Context
To determine which users are eligible to receive and donate reputation we build a context for every message which 
contains a thankword.

### Receiver context

Setting: `context receiver`, `message age` and `message min`

The receiver context defines which user can receive reputation in the thank message. To find these users we search 
the oldest message of the user which wrote the thank message inside the `message age` (Max 100 messages and 12 
hours old). All users which have written a message after this are eligible to receive reputation.

We will also add the users which wrote the last x messages defined by `message min` as long as these are not older 
than 12 hours.

Users which share or shared a voice channel in the `message age` minutes are also added to the receiver context.

### Donor context

Setting: `context donor` and `message age`

The donor context checks if the user has at least one more message in the `message age` in the channel. 
Additionally, we check if the user was in a voice channel currently or in the `message age` minutes.

## Outdated messages

Setting: `message age` and `message min`

Messages which are older than the `message age` (or older than 12 hours) are considered outdated. Messages which 
are older than `message age` and not older than 12 hours and are one of the x last messages defined by 
`message min` are never considered outdated.

## Ghost reputation

If a user thanks someone and deleted its messages the reputation will be removed as well.

## Max reputation per message

By default, the bot will limit given reputation to three users per message and embed request. You can change this 
value with the `message reputation` setting.

## Donor and Receiver limit

Setting: `limit donor` and `limit receiver`

You can limit the total given and received reputation of users with these settings. The time frame for the count is 
given in hours. You could for example define that a user can give 10 reputation in 24 hours.
