# Hosting
You can use our docker image or do manual stuff setting the bot up.

## Docker

Use the docker compose file in the docker directory.

## Normal hosting

You will need a PostgreSQL database and Java 18 or higher installed already.

## Detailed setup guide for non docker users.
We provide a bash script for setting up all requirements for the reputation bot.
We do not install any databases or java for you.

Once the setup is complete you can simply execute the start.sh to start the bot in a screen called `repbot`.

### Simple Setup
[See what's inside](https://gist.github.com/RainbowDashLabs/20d7ad959b524056a406aeb6ee53b4e2)


```sh
wget -q --no-cache https://gist.githubusercontent.com/RainbowDashLabs/20d7ad959b524056a406aeb6ee53b4e2/raw/setup.sh && chmod +x setup.sh && ./setup.sh 
```

### Manual setup
Execute the commands from this [gist](https://gist.github.com/RainbowDashLabs/20d7ad959b524056a406aeb6ee53b4e2) by hand.

## Configuration
After the first start the bot will create a configuration file.

### config/config.json
0. Create a bot account with message and guild members intent.
1. Enter your bot token
2. Adjust the database login credentials
3. Enter your user id to the bot owners

### config/log4j2.xml
1. Create a private channel with a webhook.
2. Paste the webhook url into the verbose webhook appender url field

```xml
<DiscordWebhook name="DiscordVerbose" url="<your webhook here>">
```

3. You can create additional webhooks for the two more log levels.

## Managing

The bot supports simple commands for users listed as bot owners. They can be executed by mentioning the bot and
adding the command

- `upgrade` -> Upgrade the bot on the latest stable version
- `restart` -> Restarts the bot process
- `shutdown` -> Stops the bot and ends the loop.
- `stats` -> Shows stats about bot usage
