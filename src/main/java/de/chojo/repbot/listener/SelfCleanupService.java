package de.chojo.repbot.listener;

import net.dv8tion.jda.api.sharding.ShardManager;

public class SelfCleanupService implements Runnable {

    ShardManager shardManager;

    @Override
    public void run() {
        shardManager.getGuilds();
    }
}
