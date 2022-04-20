# Get the bot

You have two option when you want the bot.

## Public instance
You can use our public instance which is managed and updated by us.

[Click here to invite the bot on your server](https://discord.com/api/oauth2/authorize?client_id=871322553698906142&permissions=1342532672&scope=bot%20applications.commands)

## Self hosting

### Docker
Use the docker compose file in the docker directory.

### Normal hosting
Build from source. No specific setup besides a JDK with an appropriate version is required to compile the bot yourself.

You will need a PostgreSQL database and Java 18 or higher. Copy the log4j config from resources to your wished
destination. I suggest a config directory

Start the bot with the following parameter. You may change the path.

``` sh
-Dlog4j.configurationFile=config/log4j2.xml -Dbot.config=config/config.json
```

After the first start a config will be created, and the bot will fail to start. Configure the bot and start it again.
