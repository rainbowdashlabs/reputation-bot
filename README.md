# Motivation
This bot was created for the [DevCord Discord Server](https://discord.gg/gfEsr79d9a). \
We needed a way to identify valuable users in our community. Most bots do these by chat activity.\
Sadly chat a high activity doesn't mean that a user is valuable for the community.\
That is where the reputation bot comes in place.

# How does it work
The reputation bot will scan messages for thank phrases, which you can define.\
When a thank phrase is found the bot will try to find the receiver of the message. This is done by several checks.
- Answer: You can answer the message which helped you with a thank word.
- Mention: You can mention the user which helped you with a thankword
- Fuzzy: You can write the name, or a part of the name relative to a thank word, and the bot will try to find the user.

As an alternative you can also give a reputation reaction to the message which helped you.

After a user has given another user a reputation he has to wait a configurable amount of minutes until he can give this user reputation again.\
Also if a message is older than a configurable amount no reputation will be given when someone answers it or adds a reaction.

You can invite the bot with this [link](https://discord.com/oauth2/authorize?client_id=834843896579489794&scope=bot&permissions=1342532672). \
Or if you dont trust me you can host it by yourself.

# Can I backfill my reputation
You can use the scan command to scan up to 100k messages in a channel for thank words. This will take some time. You can only scan one channel at a time.

# Self hosting
Use the docker compose file in the docker directory. Otherwise do the stuff below.

Build from source. You probably need to build my library on localhost currently because jitci hates gradle.

You will need a PostgreSQL database and Java 15 or higher.
Copy the log4j config from resources to your wished destination. I suggest a config directory

Start the bot with the following parameter. You may change the path.
``` sh
-Dlog4j.configurationFile=config/log4j2.xml -Dbot.config=config/config.json
```

After the first start a config will be created, and the bot will fail to start. Configure the bot and start it again.

# Other Features
There are no other features.