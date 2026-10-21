# Hosting
You can use our docker image or do manual stuff setting the bot up.

## Docker

Use the docker compose file in the docker directory.

### Upgrading from PostgreSQL 17

The compose file uses PostgreSQL 18, which stores its data in a different place than earlier versions. The data now
lives in the volume `db_data_18`. Your old data stays in `db_data` and has to be moved over once.

The volume names are prefixed with the compose project name, which is the directory name by default. Check yours with
`docker volume ls`. The commands below assume `docker_db_data`.

1. Stop the bot and the database.
   ```sh
   docker compose stop app database
   ```
2. Dump the old database with a PostgreSQL 17 container.
   ```sh
   docker run -d --name repbot-pg17 -v docker_db_data:/var/lib/postgresql/data postgres:17
   docker exec repbot-pg17 sh -c 'until pg_isready -U root; do sleep 1; done'
   docker exec repbot-pg17 pg_dump -U root -d repbot -Fc > repbot.dump
   docker rm -f repbot-pg17
   ```
3. Pull the new compose file and start only the database. It creates the new volume.
   ```sh
   docker compose up -d database
   ```
4. Restore the dump.
   ```sh
   docker compose exec -T database sh -c 'until pg_isready -U root; do sleep 1; done'
   docker compose exec -T database pg_restore -U root -d repbot --no-owner < repbot.dump
   ```
5. Start the bot.
   ```sh
   docker compose up -d
   ```

Once the bot runs fine, remove the old volume with `docker volume rm docker_db_data` and delete `repbot.dump`.

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

The bot can be managed by commands located under the bot slash command, which will be only available on the bot 
guild defined in the config file.

- `upgrade` -> Upgrade the bot on the latest stable version (Not supported when using docker)
- `restart` -> Restarts the bot process
- `shutdown` -> Stops the bot and ends the loop.
- `stats` -> Shows stats about bot usage
