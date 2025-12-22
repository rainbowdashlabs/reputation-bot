package de.chojo.repbot.service.reputation;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public record ReputationContext(GuildMessageChannel guildChannel, ISnowflake snowflake) {
    public long getIdLong() {
        return snowflake.getIdLong();
    }

    public GuildMessageChannel getChannel(){
        return guildChannel;
    }

    public Guild getGuild() {
        return guildChannel.getGuild();
    }

    public boolean isMessage(){
        return snowflake instanceof Message;
    }

    public Message asMessage(){
        return (Message) snowflake;
    }
}
