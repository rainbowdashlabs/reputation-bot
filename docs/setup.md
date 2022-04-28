# Setup

1. Head to Server Settings -> Integrations. Click to manage the reputation bot and add at least one role which 
   should be allowed to manage the bot.
2. Execute the `/setup` command and follow the instructions, which will guide you through the basic setup

After this, there are some optional steps you may want to take a look at.

- Use the `/scan` command to scan the messages in a channel for thank phrases. This will backfill your reputations. You
  can scan up to 100k messages in a channel. This will take some time and you can only scan one channel at a time.
- Set your own reputation emote with `/reaction main`. This can be a custom emote from your server or a normal
  discord emote. you can also add more additional emotes with `/reactions add`.
- Set the legacy prefix. All slash commands also exists as legacy text commands.
- Use the `/repsettings` command to tweak your settings.
- User the `/thankwords check` command to check if a message would give reputation.
