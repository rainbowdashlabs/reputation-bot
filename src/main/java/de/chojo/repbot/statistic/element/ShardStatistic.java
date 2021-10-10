package de.chojo.repbot.statistic.element;

import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.repbot.statistic.ReplacementProvider;
import net.dv8tion.jda.api.JDA;

import java.util.List;

public class ShardStatistic implements ReplacementProvider {
    private final int shard;
    private final JDA.Status status;
    private final long analyzedMessages;
    private final long guilds;

    public ShardStatistic(int shard, JDA.Status status, long analyzedMessages, long guilds) {
        this.shard = shard;
        this.status = status;
        this.analyzedMessages = analyzedMessages;
        this.guilds = guilds;
    }

    public int shard() {
        return shard;
    }

    public JDA.Status status() {
        return status;
    }

    public long analyzedMessages() {
        return analyzedMessages;
    }

    @Override
    public List<Replacement> replacements() {
        return List.of(Replacement.create("analyzed_messages_shard", analyzedMessages), Replacement.create("shard_status", status.name()),
                Replacement.create("shard_id", shard), Replacement.create("shard_guilds", guilds));
    }

    public long guilds() {
        return guilds;
    }
}
