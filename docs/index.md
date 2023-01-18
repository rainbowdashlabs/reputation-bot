Welcome on the Reputation Bot website. We provide all information about the bot, setup and usage here.

## Goal of the Reputation Bot

The Reputation Bot was created for communities where the main focus is on mutual help between users.

Normal level systems, which are based on pure activity are not able to reflect how much a user contributes to the
community. User which have a lot of knowledge and are helping all the time have the same level as users which asks a
lot of questions all the time. That's where the Reputation Bot comes in handy.

_This bot is not a simple dumb bot which counts reputation, but has also a lot of checks which ensure that the 
person is actualy elligible to receive reputation. Those checks are enabled by default, but can be disabled. See our 
[abuse protection page](abuse_protection.md) for further information._

## How does it work

The bot is triggered by so called "thankwords". These are user defined words (We provide reasonable default 
settings for every supported language) which usually are something like "Thanks" or "thx" or something completely 
different.

On receiving the thankword the bot will perform several check, which might lead to giving reputation to an
identified target. If the bot cant identify a target it will question the user which used the thankword to give
reputation to an available user.

We decided against a command for giving rep for multiple reasons:

1. Users nearly always write some thank phrase anyway, which we can use to identify targets.
2. Actively asking the user to give reputation, will remind him and make it easy to give reputation. This is less 
   work for the user. It is important to make it as easy as possible.
3. A command would be an actively reminded action by the user and can be considered as additional work besides 
   thanking the person who helped.
4. The bot is not disruptive and deletes his messages afterwards to keep the channel clean.

All in all we want to make it as easy and intuitive as possible to give reputation. It should be a no-brainer for 
the users to use and should cause as less additional actions as possible.

To see the different ways to give reputation have a look at our extra [page](give_reputation.md)

# Reputation Profile

Each user will have its own reputation profile. The top users get some nice badges on it.

![Reputation profile with badge](resources/profile.png)

# Roles

You can define multiple roles which a user will get when he has a minimum amount of reputation.  
These are displayed as level.

![A list of roles](resources/roles.png)

# Toplists and reputation modes

We provide different modes

- Total
- Month
- Week
- 7 Days
- 30 Days

Those determine the user rank, leaderboard and the reputation shown in the user profile.

# Get help

Join our discord if you need help, have questions or ideas for a new feature.

<iframe src="https://discord.com/widget?id=853250161915985958&theme=dark" width="350" height="500" allowtransparency="true" frameborder="0" sandbox="allow-popups allow-popups-to-escape-sandbox allow-same-origin allow-scripts"></iframe>

# You can invite the bot with this [link](https://discord.com/api/oauth2/authorize?client_id=871322553698906142&permissions=1342532672&scope=bot%20applications.commands).
