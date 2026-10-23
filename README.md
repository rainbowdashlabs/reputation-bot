# Reputation Bot

A Discord bot that finds out who helped whom. When someone thanks another member, by mention, reply, reaction or
just a "thanks", the bot gives that member reputation. Reputation feeds rankings, profiles and reputation roles, and a
web dashboard holds every setting. It was built for communities where helping each other matters more than chat
activity.

Documentation: https://docs.chojo.dev/reputation-bot

## Use it

[Invite the public bot](https://invite.repbot.chojo.dev) and run `/setup` on your server. The
[getting started guide](https://docs.chojo.dev/reputation-bot/getting-started/invite) walks you through it.

## Host it yourself

The bot runs from the Docker image `ghcr.io/rainbowdashlabs/reputation-bot` with a PostgreSQL database. Start from
[`docker/docker-compose.yml`](docker/docker-compose.yml) and follow the
[self-hosting guide](https://docs.chojo.dev/reputation-bot/self-hosting).

## Build from source

Requires JDK 25 and Node 24.

```bash
./gradlew build
cd frontend && npm ci && npm run build
```

The bot jar lands in `build/libs/`, the dashboard in `frontend/dist/`. `docker build .` builds both into one image.

## Licence

AGPL-3.0-only, see [LICENSE.md](LICENSE.md).
